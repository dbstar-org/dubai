package test.io.github.dbstarll.dubai.model.notify;

import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.join.CompanyBase;
import io.github.dbstarll.dubai.model.notify.*;
import junit.framework.TestCase;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TestEntityNotifyConsumer extends TestCase {
    /**
     * 测试缺省的NotifyConsumer.
     *
     * @throws IOException IOException
     */
    public void testDefaultNotifyConsumer() throws IOException {
        final EntityNotifyConsumer consumer = new EntityNotifyConsumer();
        consumer.start();
        consumer.close();
    }

    /**
     * 测试自定义的NotifyConsumer.
     *
     * @throws IOException IOException
     */
    public void testNotifyConsumer() throws IOException {
        final EntityNotifyConsumer consumer = new EntityNotifyConsumer();
        final AtomicBoolean regist = new AtomicBoolean(false);
        final AtomicBoolean unRegist = new AtomicBoolean(false);
        consumer.setNotifyConsumer(new NotifyConsumer() {
            @Override
            public void unRegist(NotifyListener listener) {
                unRegist.set(true);
            }

            @Override
            public void regist(NotifyListener listener) {
                regist.set(true);
            }
        });
        assertFalse(regist.get());
        assertFalse(unRegist.get());

        consumer.start();
        assertTrue(regist.get());
        assertFalse(unRegist.get());

        consumer.close();
        assertTrue(regist.get());
        assertTrue(unRegist.get());
    }

    /**
     * 测试onNotify方法.
     *
     * @throws IOException IOException
     */
    public void testOnNotify() throws IOException {
        final EntityNotifyConsumer consumer = new EntityNotifyConsumer();
        consumer.onNotify(SimpleEntity.class.getName(), "value1", new NotifyParser() {
            @Override
            public ObjectId getObjectId(String key) {
                return null;
            }

            @Override
            public NotifyType getNotifyType() {
                return null;
            }
        });
        consumer.onNotify(SimpleEntity.class.getName(), "value2", new NotifyParser() {
            @Override
            public ObjectId getObjectId(String key) {
                return new ObjectId();
            }

            @Override
            public NotifyType getNotifyType() {
                return null;
            }
        });
        consumer.onNotify(SimpleEntity.class.getName(), "value3", new NotifyParser() {
            @Override
            public ObjectId getObjectId(String key) {
                return null;
            }

            @Override
            public NotifyType getNotifyType() {
                return NotifyType.insert;
            }
        });
        consumer.onNotify(SimpleEntity.class.getName(), "value4", new NotifyParser() {
            @Override
            public ObjectId getObjectId(String key) {
                return new ObjectId();
            }

            @Override
            public NotifyType getNotifyType() {
                return NotifyType.insert;
            }
        });
        consumer.close();
    }

    /**
     * 测试设置listener.
     *
     * @throws IOException IOException
     */
    public void testListener() throws IOException {
        final EntityNotifyConsumer consumer = new EntityNotifyConsumer();

        final AtomicInteger calls = new AtomicInteger(0);
        final AtomicReference<Class<?>> classValue = new AtomicReference<>();
        final AtomicReference<ObjectId> idValue = new AtomicReference<>();
        final AtomicReference<NotifyType> notifyTypeValue = new AtomicReference<>();
        final AtomicReference<ObjectId> companyIdValue = new AtomicReference<>();
        final EntityNotifyListener listener = new EntityNotifyListener() {
            @Override
            public <E extends Entity> void onNotify(Class<E> entityClass, ObjectId id, NotifyType notifyType,
                                                    ObjectId companyId, String clientId) {
                calls.incrementAndGet();
                classValue.set(entityClass);
                idValue.set(id);
                notifyTypeValue.set(notifyType);
                companyIdValue.set(companyId);
            }
        };
        consumer.regist(listener);

        final ObjectId id = new ObjectId();
        final ObjectId companyId = new ObjectId();
        final NotifyType type = NotifyType.insert;
        final NotifyParser notifyParser = new NotifyParser() {
            @Override
            public ObjectId getObjectId(String key) {
                if (Entity.FIELD_NAME_ID.equals(key)) {
                    return id;
                } else if (CompanyBase.FIELD_NAME_COMPANY_ID.equals(key)) {
                    return companyId;
                } else {
                    return null;
                }
            }

            @Override
            public NotifyType getNotifyType() {
                return type;
            }
        };
        consumer.onNotify(SimpleEntity.class.getName(), "value4", notifyParser);
        consumer.onNotify(String.class.getName(), "value", notifyParser);
        consumer.onNotify("io.github.dbstarll.dubai.notify.UnknownClass", "value", notifyParser);
        consumer.unRegist(listener);
        consumer.close();

        assertEquals(1, calls.get());
        assertEquals(SimpleEntity.class, classValue.get());
        assertEquals(id, idValue.get());
        assertEquals(type, notifyTypeValue.get());
        assertEquals(companyId, companyIdValue.get());
    }

    /**
     * 测试设置listener抛出异常.
     *
     * @throws IOException IOException
     */
    public void testListenerException() throws IOException {
        final EntityNotifyConsumer consumer = new EntityNotifyConsumer();

        final EntityNotifyListener listener = new EntityNotifyListener() {
            @Override
            public <E extends Entity> void onNotify(Class<E> entityClass, ObjectId id, NotifyType notifyType,
                                                    ObjectId companyId, String clientId) {
                throw new RuntimeException("test");
            }
        };
        consumer.regist(listener);

        final ObjectId id = new ObjectId();
        final ObjectId companyId = new ObjectId();
        final NotifyType type = NotifyType.insert;
        final NotifyParser notifyParser = new NotifyParser() {
            @Override
            public ObjectId getObjectId(String key) {
                if (Entity.FIELD_NAME_ID.equals(key)) {
                    return id;
                } else if (CompanyBase.FIELD_NAME_COMPANY_ID.equals(key)) {
                    return companyId;
                } else {
                    return null;
                }
            }

            @Override
            public NotifyType getNotifyType() {
                return type;
            }
        };
        consumer.onNotify(SimpleEntity.class.getName(), "value4", notifyParser);
        consumer.unRegist(listener);
        consumer.close();
    }
}
