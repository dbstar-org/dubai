package io.github.dbstarll.dubai.model.service;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.service.ServiceFactory.GeneralValidateable;
import io.github.dbstarll.dubai.model.service.ServiceFactory.PositionValidation;
import io.github.dbstarll.dubai.model.service.ServiceFactory.ServiceProxy;
import io.github.dbstarll.dubai.model.service.test.AbstractClassService;
import io.github.dbstarll.dubai.model.service.test.ClassService;
import io.github.dbstarll.dubai.model.service.test.InterfaceService;
import io.github.dbstarll.dubai.model.service.test.NoAnnotationClassService;
import io.github.dbstarll.dubai.model.service.test.NoAnnotationInterfaceService;
import io.github.dbstarll.dubai.model.service.test.PrivateClassService;
import io.github.dbstarll.dubai.model.service.test.TestServices;
import io.github.dbstarll.dubai.model.service.test.ThrowClassService;
import io.github.dbstarll.dubai.model.service.test4.TestValidEntity;
import io.github.dbstarll.dubai.model.service.test4.TestValidService;
import io.github.dbstarll.dubai.model.service.test5.ImplFailedEntity;
import io.github.dbstarll.dubai.model.service.test5.ImplFailedImplemental;
import io.github.dbstarll.dubai.model.service.test5.ImplFailedService;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestServiceFactory extends ServiceTestCase {
    private final Class<InterfaceEntity> entityClass = InterfaceEntity.class;

    @BeforeClass
    public static void setup() {
        MongodTestCase.globalCollectionFactory();
    }

    @Test
    public void testGetInterfaceService() {
        useService(InterfaceService.class, s -> {
            assertEquals(entityClass, s.getEntityClass());
            assertEquals(InterfaceService.class, ServiceFactory.getServiceClass(s));
        });
    }

    @Test
    public void testGetNoAnnotationInterfaceService() {
        useCollection(entityClass, c -> {
            try {
                ServiceFactory.newInstance(NoAnnotationInterfaceService.class, c);
                fail("throw UnsupportedOperationException");
            } catch (Exception ex) {
                assertEquals(UnsupportedOperationException.class, ex.getClass());
                assertEquals("Invalid ServiceClass: " + NoAnnotationInterfaceService.class, ex.getMessage());
            }
        });
    }

    @Test
    public void testGetClassService() {
        useCollection(entityClass, c -> {
            final ClassService service = ServiceFactory.newInstance(ClassService.class, c);
            assertEquals(entityClass, service.getEntityClass());
            assertEquals(ClassService.class, ServiceFactory.getServiceClass(service));
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAbstractClassService() {
        useCollection(entityClass, c -> {
            try {
                ServiceFactory.newInstance(AbstractClassService.class, c);
                fail("throw UnsupportedOperationException");
            } catch (Exception ex) {
                assertEquals(UnsupportedOperationException.class, ex.getClass());
                assertEquals("Invalid ServiceClass: " + AbstractClassService.class, ex.getMessage());
            }
        });
    }

    @Test
    public void testGetPrivateClassService() {
        useCollection(entityClass, c -> {
            try {
                ServiceFactory.newInstance(PrivateClassService.class, c);
                fail("throw UnsupportedOperationException");
            } catch (Exception ex) {
                assertEquals(UnsupportedOperationException.class, ex.getClass());
                assertEquals("Invalid ServiceClass: " + PrivateClassService.class, ex.getMessage());
            }
        });
    }

    @Test
    public void testThrowClassService() {
        useCollection(entityClass, c -> {
            try {
                ServiceFactory.newInstance(ThrowClassService.class, c);
                fail("throw UnsupportedOperationException");
            } catch (Exception ex) {
                assertEquals(UnsupportedOperationException.class, ex.getClass());
                assertEquals("Instantiation fails: " + ThrowClassService.class, ex.getMessage());
            }
        });
    }

    @Test
    public void testNoAnnotationClassService() {
        useCollection(entityClass, c -> {
            try {
                ServiceFactory.newInstance(NoAnnotationClassService.class, c);
                fail("throw UnsupportedOperationException");
            } catch (Exception ex) {
                assertEquals(UnsupportedOperationException.class, ex.getClass());
                assertEquals("Invalid ServiceClass: " + NoAnnotationClassService.class, ex.getMessage());
            }
        });
    }

    /**
     * 测试非ServiceFactory的代理类调用getServiceClass.
     */
    @Test
    public void testGetServiceClassNoServiceFactory() {
        useCollection(entityClass, c -> {
            final InterfaceService service = (InterfaceService) Proxy.newProxyInstance(InterfaceService.class.getClassLoader(),
                    new Class[]{InterfaceService.class, ServiceProxy.class}, (proxy, method, args) -> null);
            assertNotEquals(InterfaceService.class, ServiceFactory.getServiceClass(service));
            assertEquals(service.getClass(), ServiceFactory.getServiceClass(service));
        });
    }

    @Test
    public void testGetServiceClassNoServiceProxy() {
        useCollection(entityClass, c -> {
            final InterfaceService service = (InterfaceService) Proxy.newProxyInstance(InterfaceService.class.getClassLoader(),
                    new Class[]{InterfaceService.class}, (proxy, method, args) -> null);
            assertNotEquals(InterfaceService.class, ServiceFactory.getServiceClass(service));
            assertEquals(service.getClass(), ServiceFactory.getServiceClass(service));
        });
    }

    @Test
    public void testHashCode() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            assertNotEquals(0, service.hashCode());
        });
    }

    @Test
    public void testSetImplementalAutowirer() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            ((ImplementalAutowirerAware) service).setImplementalAutowirer(null);

            assertEquals(0, service.count(Filters.eq(new ObjectId())));
        });
    }

    @Test
    public void testInvokeUnImplementation() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            try {
                service.unImplementation();
                fail("throw UnsupportedOperationException");
            } catch (Exception e) {
                assertEquals(UnsupportedOperationException.class, e.getClass());
                final Method method;
                try {
                    method = InterfaceService.class.getMethod("unImplementation");
                } catch (NoSuchMethodException ex) {
                    throw new IllegalStateException(ex);
                }
                assertEquals(method.toString(), e.getMessage());
            }
        });
    }

    @Test
    public void testOverrideMethod() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            assertTrue(service.contains(new ObjectId()));
        });
    }

    @Test
    public void testFailedMethod() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            try {
                service.failed();
                fail("throw UnsupportedOperationException");
            } catch (Exception e) {
                assertEquals(UnsupportedOperationException.class, e.getClass());
                final Method method;
                try {
                    method = InterfaceService.class.getMethod("failed");
                } catch (NoSuchMethodException ex) {
                    throw new IllegalStateException(ex);
                }
                assertEquals(method.toString(), e.getMessage());
            }
        });
    }

    @Test
    public void testThrowExceptionMethod() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            try {
                service.throwException();
                fail("throw IllegalAccessException");
            } catch (Exception e) {
                assertEquals(IllegalAccessException.class, e.getClass());
                assertEquals("throwException", e.getMessage());
            }
        });
    }

    @Test
    public void testNotPublicImplementation() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            try {
                service.notPublic();
                fail("throw UnsupportedOperationException");
            } catch (Exception e) {
                assertEquals(UnsupportedOperationException.class, e.getClass());
                final Method method;
                try {
                    method = InterfaceService.class.getMethod("notPublic");
                } catch (NoSuchMethodException ex) {
                    throw new IllegalStateException(ex);
                }
                assertEquals(method.toString(), e.getMessage());
            }
        });
    }

    @Test
    public void testNotFinalImplementation() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            try {
                service.notFinal();
                fail("throw UnsupportedOperationException");
            } catch (Exception e) {
                assertEquals(UnsupportedOperationException.class, e.getClass());
                final Method method;
                try {
                    method = InterfaceService.class.getMethod("notFinal");
                } catch (NoSuchMethodException ex) {
                    throw new IllegalStateException(ex);
                }
                assertEquals(method.toString(), e.getMessage());
            }
        });
    }

    @Test
    public void testCreateImplementationWithException() {
        useCollection(ImplFailedEntity.class, c -> {
            final ImplFailedService service = ServiceFactory.newInstance(ImplFailedService.class, c);
            try {
                service.done();
                fail("throw AutowireException");
            } catch (Exception e) {
                e.printStackTrace();
                assertEquals(ImplementalInstanceException.class, e.getClass());
                assertEquals("不能实例化Implemental：" + ImplFailedImplemental.class.getName(), e.getMessage());
                assertNotNull(e.getCause());
                assertEquals(RuntimeException.class, e.getCause().getClass());
                assertEquals("ImplFailed", e.getCause().getMessage());
            }
        });
    }

    @Test
    public void testAutowireException() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            ((ImplementalAutowirerAware) service).setImplementalAutowirer(new ImplementalAutowirer() {
                @Override
                public <I extends Implemental> void autowire(I implemental) throws AutowireException {
                    throw new AutowireException("autowireBeanProperties");
                }
            });
            try {
                service.contains(new ObjectId());
                fail("throw AutowireException");
            } catch (Exception e) {
                assertEquals(AutowireException.class, e.getClass());
                assertEquals("autowireBeanProperties", e.getMessage());
            }
        });
    }

    @Test
    public void testCall() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            try {
                service.callTest(EntityFactory.newInstance(entityClass));
                fail("throw UnsupportedOperationException");
            } catch (Exception e) {
                assertEquals(UnsupportedOperationException.class, e.getClass());
                final Method method;
                try {
                    method = InterfaceService.class.getMethod("callTest", Entity.class);
                } catch (NoSuchMethodException ex) {
                    throw new IllegalStateException(ex);
                }
                assertEquals(method.toString(), e.getMessage());
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGeneralValidation() {
        useCollection(entityClass, c -> {
            final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, c);
            assertTrue(service instanceof GeneralValidateable);
            final java.util.Collection<PositionValidation<InterfaceEntity>> validation =
                    ((GeneralValidateable<InterfaceEntity>) service).generalValidations();
            assertNotNull(validation);
            final java.util.Collection<PositionValidation<InterfaceEntity>> validation2 =
                    ((GeneralValidateable<InterfaceEntity>) service).generalValidations();
            assertSame(validation, validation2);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGeneralValidationThrow() {
        useCollection(TestValidEntity.class, c -> {
            final TestValidService service = ServiceFactory.newInstance(TestValidService.class, c);
            try {
                ((GeneralValidateable<TestValidEntity>) service).generalValidations();
                fail("throw UnsupportedOperationException");
            } catch (Throwable ex) {
                assertEquals(UnsupportedOperationException.class, ex.getClass());
                assertEquals("throwValidation", ex.getMessage());
                assertNull(ex.getCause());
            }
        });
    }

    @Test
    public void testGetServiceClassNoProxy() {
        assertSame(String.class, ServiceFactory.getServiceClass(String.class));
    }

    @Test
    public void testIsServiceInterface() {
        assertFalse(ServiceFactory.isServiceInterface(String.class));
        assertTrue(ServiceFactory.isServiceInterface(InterfaceService.class));
        assertFalse(ServiceFactory.isServiceInterface(ClassService.class));
    }

    @Test
    public void testIsServiceProxy() {
        useCollection(InterfaceEntity.class, c -> {
            assertFalse(ServiceFactory.isServiceProxy(String.class));
            assertTrue(ServiceFactory.isServiceProxy(ServiceFactory.newInstance(InterfaceService.class, c).getClass()));

            final NoAnnotationInterfaceService service = (NoAnnotationInterfaceService) Proxy.newProxyInstance(
                    NoAnnotationInterfaceService.class.getClassLoader(),
                    new Class[]{NoAnnotationInterfaceService.class, ServiceProxy.class}, (proxy, method, args) -> null);
            assertFalse(ServiceFactory.isServiceProxy(service.getClass()));
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetEntityClass() {
        assertSame(InterfaceEntity.class, ServiceFactory.getEntityClass(InterfaceService.class));
        assertSame(InterfaceEntity.class, ServiceFactory.getEntityClass(ClassService.class));
        assertNull(ServiceFactory.getEntityClass(TestServices.class));
    }
}
