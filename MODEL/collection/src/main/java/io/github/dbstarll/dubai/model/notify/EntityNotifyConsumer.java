package io.github.dbstarll.dubai.model.notify;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.join.CompanyBase;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class EntityNotifyConsumer implements NotifyListener, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityNotifyConsumer.class);

    private NotifyConsumer notifyConsumer = new DefaultNotifyConsumer();

    private final List<EntityNotifyListener> listeners = new LinkedList<>();
    private final ConcurrentMap<String, Class<? extends Entity>> entityClasses = new ConcurrentHashMap<>();

    public void setNotifyConsumer(NotifyConsumer notifyConsumer) {
        this.notifyConsumer = notifyConsumer;
    }

    public void start() {
        notifyConsumer.regist(this);
    }

    @Override
    public void close() throws IOException {
        notifyConsumer.unRegist(this);
    }

    @Override
    public void onNotify(String key, String value, NotifyParser parser) {
        final Class<? extends Entity> entityClass = getEntityClass(key);
        if (NullEntity.class != entityClass) {
            final ObjectId id = parser.getObjectId(Entity.FIELD_NAME_ID);
            final ObjectId companyId = parser.getObjectId(CompanyBase.FIELD_NAME_COMPANY_ID);
            final NotifyType notifyType = parser.getNotifyType();
            if (id != null && notifyType != null) {
                onNotify(entityClass, id, notifyType, companyId, value);
            }
        }
    }

    private void onNotify(Class<? extends Entity> entityClass, ObjectId id, NotifyType notifyType, ObjectId companyId,
                          String clientId) {
        LOGGER.debug("{} - {}@{}, companyId: {}", notifyType, entityClass.getName(), id, companyId);
        synchronized (listeners) {
            for (EntityNotifyListener listener : listeners) {
                try {
                    listener.onNotify(entityClass, id, notifyType, companyId, clientId);
                } catch (Throwable ex) {
                    LOGGER.error("entity notify failed: " + listener, ex);
                }
            }
        }
    }

    /**
     * 注册EntityNotifyListener.
     *
     * @param listener 注册的监听器
     */
    public void regist(EntityNotifyListener listener) {
        synchronized (listeners) {
            LOGGER.info("regist: {}: {}", listener, listeners.add(listener));
        }
    }

    /**
     * 注销EntityNotifyListener.
     *
     * @param listener 注销的监听器
     */
    public void unRegist(EntityNotifyListener listener) {
        synchronized (listeners) {
            LOGGER.info("unRegist: {}: {}", listener, listeners.remove(listener));
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Entity> getEntityClass(String className) {
        if (!entityClasses.containsKey(className)) {
            try {
                final Class<?> entityClass = getClass().getClassLoader().loadClass(className);
                if (Entity.class.isAssignableFrom(entityClass)) {
                    entityClasses.putIfAbsent(className, (Class<? extends Entity>) entityClass);
                } else {
                    LOGGER.warn("Unknown Entity Class: {}", entityClass);
                    entityClasses.putIfAbsent(className, NullEntity.class);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.warn("ClassNotFoundException: {}", className);
                entityClasses.putIfAbsent(className, NullEntity.class);
            }
        }

        return entityClasses.get(className);
    }

    private interface NullEntity extends Entity {
    }

    private static class DefaultNotifyConsumer implements NotifyConsumer {
        @Override
        public void regist(NotifyListener listener) {
        }

        @Override
        public void unRegist(NotifyListener listener) {
        }
    }
}
