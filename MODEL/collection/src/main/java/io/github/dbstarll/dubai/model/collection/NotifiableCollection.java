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

    public void setNotifyProvider(NotifyProvider notifyProvider) {
        this.notifyProvider = notifyProvider;
    }

    public NotifiableCollection(Collection<E> collection) {
        super(collection);
    }

    @Override
    public void insertOne(E document, InsertOneOptions options) {
        super.insertOne(document, options);
        doNotify(document, NotifyType.insert);
    }

    @Override
    public void insertMany(List<? extends E> documents, InsertManyOptions options) {
        try {
            super.insertMany(documents, options);
        } finally {
            doNotify(documents, NotifyType.insert);
        }
    }

    @Override
    public UpdateResult updateOne(Bson filter, Bson update, UpdateOptions updateOptions) {
        E document = findOne(filter);

        final UpdateResult result = super.updateOne(filter, update, updateOptions);
        if (update == DefunctableCollection.DEFUNCT) {
            doNotify(document, NotifyType.delete);
        } else {
            doNotify(document, NotifyType.update);
        }
        return result;
    }

    @Override
    public UpdateResult updateMany(Bson filter, Bson update, UpdateOptions updateOptions) {
        List<E> documents = toList(find(filter));

        final UpdateResult result = super.updateMany(filter, update, updateOptions);
        if (update == DefunctableCollection.DEFUNCT) {
            doNotify(documents, NotifyType.delete);
        } else {
            doNotify(documents, NotifyType.update);
        }
        return result;
    }

    @Override
    public UpdateResult replaceOne(Bson filter, E replacement, ReplaceOptions replaceOptions) {
        final UpdateResult result = super.replaceOne(filter, replacement, replaceOptions);
        doNotify(replacement, NotifyType.update);
        return result;
    }

    @Override
    public DeleteResult deleteOne(Bson filter, DeleteOptions options) {
        E document = findOne(filter);

        final DeleteResult result = super.deleteOne(filter, options);
        doNotify(document, NotifyType.delete);
        return result;
    }

    @Override
    public DeleteResult deleteMany(Bson filter, DeleteOptions options) {
        List<E> documents = toList(find(filter));

        final DeleteResult result = super.deleteMany(filter, options);
        doNotify(documents, NotifyType.delete);
        return result;
    }

    @Override
    public E findOneAndDelete(Bson filter, FindOneAndDeleteOptions options) {
        final E entity = findOne(filter);
        final E removed = super.findOneAndDelete(filter, options);
        doNotify(entity, NotifyType.delete);
        return removed;
    }

    @Override
    public E findOneAndReplace(Bson filter, E replacement, FindOneAndReplaceOptions options) {
        final E entity = findOne(filter);
        final E replaced = super.findOneAndReplace(filter, replacement, options);
        doNotify(entity, NotifyType.update);
        return replaced;
    }

    @Override
    public E findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
        final E entity = findOne(filter);
        final E updated = super.findOneAndUpdate(filter, update, options);
        if (update == DefunctableCollection.DEFUNCT) {
            doNotify(entity, NotifyType.delete);
        } else {
            doNotify(entity, NotifyType.update);
        }
        return updated;
    }

    private void doNotify(E document, NotifyType notifyType) {
        notifyProvider.doNotify(document, notifyType);
    }

    private void doNotify(Iterable<? extends E> documents, NotifyType notifyType) {
        for (E e : documents) {
            doNotify(e, notifyType);
        }
    }

    private static <E extends Entity> List<E> toList(FindIterable<E> iterable) {
        List<E> ret = new ArrayList<>();
        iterable.iterator().forEachRemaining(e -> ret.add(e));
        return ret;
    }

    private static class DefaultNotifyProvider implements NotifyProvider {
        @Override
        public <E extends Entity> void doNotify(E e, NotifyType notifyType) {
            // do nothing
        }
    }
}
