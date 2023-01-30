package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.notify.NotifyProvider;
import io.github.dbstarll.dubai.model.notify.NotifyType;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class NotifiableCollection<E extends Entity> extends CollectionWrapper<E> {
    private NotifyProvider notifyProvider = new DefaultNotifyProvider();

    /**
     * 设置通知提供者，用于发送实体更新通知.
     *
     * @param notifyProvider 通知提供者
     */
    public void setNotifyProvider(final NotifyProvider notifyProvider) {
        this.notifyProvider = notifyProvider;
    }

    /**
     * 封装一个collection，并叠加实体更新通知功能.
     *
     * @param collection 被封装的collection
     */
    public NotifiableCollection(final Collection<E> collection) {
        super(collection);
    }

    @Override
    public void insertOne(final E document, final InsertOneOptions options) {
        super.insertOne(document, options);
        doNotify(document, NotifyType.INSERT);
    }

    @Override
    public void insertMany(final List<? extends E> documents, final InsertManyOptions options) {
        try {
            super.insertMany(documents, options);
        } finally {
            doNotify(documents, NotifyType.INSERT);
        }
    }

    @Override
    public UpdateResult updateOne(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        E document = findOne(filter);

        final UpdateResult result = super.updateOne(filter, update, updateOptions);
        if (update == DefunctableCollection.DEFUNCT) {
            doNotify(document, NotifyType.DELETE);
        } else {
            doNotify(document, NotifyType.UPDATE);
        }
        return result;
    }

    @Override
    public UpdateResult updateMany(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        List<E> documents = toList(find(filter));

        final UpdateResult result = super.updateMany(filter, update, updateOptions);
        if (update == DefunctableCollection.DEFUNCT) {
            doNotify(documents, NotifyType.DELETE);
        } else {
            doNotify(documents, NotifyType.UPDATE);
        }
        return result;
    }

    @Override
    public UpdateResult replaceOne(final Bson filter, final E replacement, final ReplaceOptions replaceOptions) {
        final UpdateResult result = super.replaceOne(filter, replacement, replaceOptions);
        doNotify(replacement, NotifyType.UPDATE);
        return result;
    }

    @Override
    public DeleteResult deleteOne(final Bson filter, final DeleteOptions options) {
        E document = findOne(filter);

        final DeleteResult result = super.deleteOne(filter, options);
        doNotify(document, NotifyType.DELETE);
        return result;
    }

    @Override
    public DeleteResult deleteMany(final Bson filter, final DeleteOptions options) {
        List<E> documents = toList(find(filter));

        final DeleteResult result = super.deleteMany(filter, options);
        doNotify(documents, NotifyType.DELETE);
        return result;
    }

    @Override
    public E findOneAndDelete(final Bson filter, final FindOneAndDeleteOptions options) {
        final E entity = findOne(filter);
        final E removed = super.findOneAndDelete(filter, options);
        doNotify(entity, NotifyType.DELETE);
        return removed;
    }

    @Override
    public E findOneAndReplace(final Bson filter, final E replacement, final FindOneAndReplaceOptions options) {
        final E entity = findOne(filter);
        final E replaced = super.findOneAndReplace(filter, replacement, options);
        doNotify(entity, NotifyType.UPDATE);
        return replaced;
    }

    @Override
    public E findOneAndUpdate(final Bson filter, final Bson update, final FindOneAndUpdateOptions options) {
        final E entity = findOne(filter);
        final E updated = super.findOneAndUpdate(filter, update, options);
        if (update == DefunctableCollection.DEFUNCT) {
            doNotify(entity, NotifyType.DELETE);
        } else {
            doNotify(entity, NotifyType.UPDATE);
        }
        return updated;
    }

    private void doNotify(final E document, final NotifyType notifyType) {
        notifyProvider.doNotify(document, notifyType);
    }

    private void doNotify(final Iterable<? extends E> documents, final NotifyType notifyType) {
        for (E e : documents) {
            doNotify(e, notifyType);
        }
    }

    private static <E extends Entity> List<E> toList(final FindIterable<E> iterable) {
        List<E> ret = new ArrayList<>();
        iterable.iterator().forEachRemaining(ret::add);
        return ret;
    }

    private static class DefaultNotifyProvider implements NotifyProvider {
        @Override
        public <E extends Entity> void doNotify(final E e, final NotifyType notifyType) {
            // do nothing
        }
    }
}
