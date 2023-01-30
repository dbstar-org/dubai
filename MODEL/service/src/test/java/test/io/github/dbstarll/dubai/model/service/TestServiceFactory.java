package test.io.github.dbstarll.dubai.model.service;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.service.AutowireException;
import io.github.dbstarll.dubai.model.service.Implemental;
import io.github.dbstarll.dubai.model.service.ImplementalAutowirer;
import io.github.dbstarll.dubai.model.service.ImplementalAutowirerAware;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import io.github.dbstarll.dubai.model.service.ServiceFactory.GeneralValidateable;
import io.github.dbstarll.dubai.model.service.ServiceFactory.PositionValidation;
import io.github.dbstarll.dubai.model.service.test.AbstractClassService;
import io.github.dbstarll.dubai.model.service.test.ClassService;
import io.github.dbstarll.dubai.model.service.test.InterfaceService;
import io.github.dbstarll.dubai.model.service.test.NoAnnotationClassService;
import io.github.dbstarll.dubai.model.service.test.NoAnnotationInterfaceService;
import io.github.dbstarll.dubai.model.service.test.PrivateClassService;
import io.github.dbstarll.dubai.model.service.test.ThrowClassService;
import io.github.dbstarll.dubai.model.service.test4.TestValidEntity;
import io.github.dbstarll.dubai.model.service.test4.TestValidService;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestServiceFactory {
    @Mocked
    Collection<InterfaceEntity> collection;
    @Mocked
    Collection<TestValidEntity> validCollection;

    @Test
    public void testGetInterfaceService() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        assertEquals(InterfaceEntity.class, service.getEntityClass());
        assertEquals(InterfaceService.class, ServiceFactory.getServiceClass(service));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
            }
        };
    }

    @Test
    public void testGetNoAnnotationInterfaceService() {
        try {
            ServiceFactory.newInstance(NoAnnotationInterfaceService.class, collection);
            fail("throw UnsupportedOperationException");
        } catch (Exception ex) {
            assertEquals(UnsupportedOperationException.class, ex.getClass());
            assertEquals("Invalid ServiceClass: " + NoAnnotationInterfaceService.class, ex.getMessage());
        }
    }

    @Test
    public void testGetClassService() {
        final ClassService service = ServiceFactory.newInstance(ClassService.class, collection);
        assertEquals(InterfaceEntity.class, service.getEntityClass());
        assertEquals(ClassService.class, ServiceFactory.getServiceClass(service));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 0;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAbstractClassService() {
        try {
            ServiceFactory.newInstance(AbstractClassService.class, collection);
            fail("throw UnsupportedOperationException");
        } catch (Exception ex) {
            assertEquals(UnsupportedOperationException.class, ex.getClass());
            assertEquals("Invalid ServiceClass: " + AbstractClassService.class, ex.getMessage());
        }
    }

    @Test
    public void testGetPrivateClassService() {
        try {
            ServiceFactory.newInstance(PrivateClassService.class, collection);
            fail("throw UnsupportedOperationException");
        } catch (Exception ex) {
            assertEquals(UnsupportedOperationException.class, ex.getClass());
            assertEquals("Invalid ServiceClass: " + PrivateClassService.class, ex.getMessage());
        }
    }

    @Test
    public void testThrowClassService() {
        try {
            ServiceFactory.newInstance(ThrowClassService.class, collection);
            fail("throw UnsupportedOperationException");
        } catch (Exception ex) {
            assertEquals(UnsupportedOperationException.class, ex.getClass());
            assertEquals("Instantiation fails: " + ThrowClassService.class, ex.getMessage());
        }
    }

    @Test
    public void testNoAnnotationClassService() {
        try {
            ServiceFactory.newInstance(NoAnnotationClassService.class, collection);
            fail("throw UnsupportedOperationException");
        } catch (Exception ex) {
            assertEquals(UnsupportedOperationException.class, ex.getClass());
            assertEquals("Invalid ServiceClass: " + NoAnnotationClassService.class, ex.getMessage());
        }
    }

    /**
     * 测试非ServiceFactory的代理类调用getServiceClass.
     */
    @Test
    public void testGetServiceClassNoServiceFactory() {
        final InterfaceService service = (InterfaceService) Proxy.newProxyInstance(InterfaceService.class.getClassLoader(),
                new Class[]{InterfaceService.class, ImplementalAutowirerAware.class, GeneralValidateable.class},
                (proxy, method, args) -> null);
        assertNotEquals(InterfaceService.class, ServiceFactory.getServiceClass(service));
        assertEquals(service.getClass(), ServiceFactory.getServiceClass(service));
    }

    @Test
    public void testHashCode() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        assertNotEquals(0, service.hashCode());

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
            }
        };
    }

    @Test
    public void testSetImplementalAutowirer() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
                collection.count((Bson) any);
                result = 10;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        ((ImplementalAutowirerAware) service).setImplementalAutowirer(null);

        assertEquals(10, service.count(Filters.eq(new ObjectId())));
        assertEquals(10, service.count(Filters.eq(new ObjectId())));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
                collection.count((Bson) any);
                times = 2;
            }
        };
    }

    @Test
    public void testInvokeUnImplementation() throws Exception {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        try {
            service.unImplementation();
            fail("throw UnsupportedOperationException");
        } catch (Exception e) {
            assertEquals(UnsupportedOperationException.class, e.getClass());
            assertEquals(InterfaceService.class.getMethod("unImplementation").toString(), e.getMessage());
        }

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
            }
        };
    }

    @Test
    public void testOverrideMethod() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);

        assertTrue(service.contains(new ObjectId()));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
                collection.count((Bson) any);
                times = 0;
            }
        };
    }

    @Test
    public void testFailedMethod() throws Exception {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        try {
            service.failed();
            fail("throw UnsupportedOperationException");
        } catch (Exception e) {
            assertEquals(UnsupportedOperationException.class, e.getClass());
            assertEquals(InterfaceService.class.getMethod("failed").toString(), e.getMessage());
        }

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
            }
        };
    }

    @Test
    public void testThrowExceptionMethod() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        try {
            service.throwException();
            fail("throw IllegalAccessException");
        } catch (Exception e) {
            assertEquals(IllegalAccessException.class, e.getClass());
            assertEquals("throwException", e.getMessage());
        }

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
            }
        };
    }

    @Test
    public void testNotPublicImplementation() throws Exception {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        try {
            service.notPublic();
            fail("throw UnsupportedOperationException");
        } catch (Exception e) {
            assertEquals(UnsupportedOperationException.class, e.getClass());
            assertEquals(InterfaceService.class.getMethod("notPublic").toString(), e.getMessage());
        }

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
            }
        };
    }

    @Test
    public void testNotFinalImplementation() throws Exception {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        try {
            service.notFinal();
            fail("throw UnsupportedOperationException");
        } catch (Exception e) {
            assertEquals(UnsupportedOperationException.class, e.getClass());
            assertEquals(InterfaceService.class.getMethod("notFinal").toString(), e.getMessage());
        }

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
            }
        };
    }

    @Test
    public void testCreateImplementationWithException() throws Exception {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        ((ImplementalAutowirerAware) service).setImplementalAutowirer(new ImplementalAutowirer() {
            @Override
            public <I extends Implemental> void autowire(I implemental) throws AutowireException {
                throw new AutowireException("autowireBeanProperties");
            }
        });
        try {
            service.contains(new ObjectId());
            fail("throw UnsupportedOperationException");
        } catch (Exception e) {
            assertEquals(UnsupportedOperationException.class, e.getClass());
            assertEquals(InterfaceService.class.getMethod("contains", ObjectId.class).toString(), e.getMessage());
        }

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
            }
        };
    }

    @Test
    public void testCall() throws Exception {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };

        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        try {
            service.callTest(EntityFactory.newInstance(InterfaceEntity.class));
            fail("throw UnsupportedOperationException");
        } catch (Exception e) {
            assertEquals(UnsupportedOperationException.class, e.getClass());
            assertEquals(InterfaceService.class.getMethod("callTest", Entity.class).toString(), e.getMessage());
        }

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
            }
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testGeneralValidation() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = InterfaceEntity.class;
            }
        };
        final InterfaceService service = ServiceFactory.newInstance(InterfaceService.class, collection);
        assertTrue(service instanceof GeneralValidateable);
        final java.util.Collection<PositionValidation<InterfaceEntity>> validation = ((GeneralValidateable) service)
                .generalValidations();
        assertNotNull(validation);
        final java.util.Collection<PositionValidation<InterfaceEntity>> validation2 = ((GeneralValidateable) service)
                .generalValidations();
        assertSame(validation, validation2);
        new Verifications() {
            {
                collection.getEntityClass();
                times = 3;
            }
        };
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGeneralValidationThrow() {
        new Expectations() {
            {
                validCollection.getEntityClass();
                result = TestValidEntity.class;
            }
        };
        final TestValidService service = ServiceFactory.newInstance(TestValidService.class, validCollection);
        try {
            ((GeneralValidateable) service).generalValidations();
            fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            assertEquals(UnsupportedOperationException.class, ex.getClass());
            assertEquals("throwValidation", ex.getMessage());
            assertNull(ex.getCause());
        }
        new Verifications() {
            {
                validCollection.getEntityClass();
                times = 2;
            }
        };
    }
}
