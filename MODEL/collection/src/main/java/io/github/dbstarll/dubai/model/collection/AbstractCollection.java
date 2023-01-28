package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import org.bson.types.ObjectId;

import java.util.Date;

public abstract class AbstractCollection<E extends Entity> implements Collection<E> {
    @Override
    public final E save(final E entity, final ObjectId newEntityId) {
        if (entity == null) {
            return null;
        }

        final Date now = new Date();
        setEntityLastModified(entity, now);

        if (entity.getId() == null) {
            setEntityId(entity, newEntityId == null ? new ObjectId() : newEntityId);
            setEntityDateCreated(entity, newEntityId == null ? now : newEntityId.getDate());

            this.insertOne(entity);
        } else {
            this.replaceOne(Filters.eq(Entity.FIELD_NAME_ID, entity.getId()), entity);
        }

        return entity;
    }

    /**
     * 设置实体ID.
     *
     * @param entity 被设置的实体
     * @param id     被设置的ID
     * @param <E>    实体类型
     */
    private static <E extends Entity> void setEntityId(final E entity, final ObjectId id) {
        ((EntityModifier) entity).setId(id);
    }

    /**
     * 设置实体的创建时间.
     *
     * @param entity      被设置的实体
     * @param dateCreated 被设置的创建时间
     * @param <E>         实体类型
     */
    private static <E extends Entity> void setEntityDateCreated(final E entity, final Date dateCreated) {
        ((EntityModifier) entity).setDateCreated(dateCreated);
    }

    /**
     * 设置实体的最后修改时间.
     *
     * @param entity       被设置的实体
     * @param lastModified 被设置的最后修改时间
     * @param <E>          实体类型
     */
    private static <E extends Entity> void setEntityLastModified(final E entity, final Date lastModified) {
        if (entity instanceof EntityModifier) {
            ((EntityModifier) entity).setLastModified(lastModified);
        } else {
            throw new IllegalArgumentException("UnModify entity: " + entity.getClass().getName());
        }
    }
}
