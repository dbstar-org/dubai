package io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.service.AutowireException;
import io.github.dbstarll.dubai.model.service.Implemental;
import junit.framework.TestCase;
import org.bson.types.ObjectId;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyBatchUpdateException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.StaticApplicationContext;

import java.util.concurrent.atomic.AtomicReference;

public class TestSpringImplementalAutowirer extends TestCase {

    /**
     * 测试未设置ApplicationContext.
     */
    public void testNoApplicationContext() {
        try {
            new SpringImplementalAutowirer().autowire(new Implemental() {
            });
            fail("throw AutowireException");
        } catch (Throwable ex) {
            assertEquals(AutowireException.class, ex.getClass());
            assertNull(ex.getCause());
            assertEquals("AutowireCapableBeanFactory not set.", ex.getMessage());
        }
    }

    /**
     * 测试设置ApplicationContext.
     */
    public void testApplicationContext() {
        final StaticApplicationContext context = new StaticApplicationContext();
        context.registerPrototype("objectId", ObjectId.class);
        final SpringImplementalAutowirer autowirer = new SpringImplementalAutowirer();
        autowirer.setApplicationContext(context);
        final AtomicReference<ObjectId> idRef = new AtomicReference<>();
        final Implemental implemental = new Implemental() {
            public void setObjectId(ObjectId id) {
                idRef.set(id);
            }
        };

        autowirer.autowire(implemental);
        assertNotNull(idRef.get());
    }

    /**
     * 测试抛出BeansException.
     */
    public void testBeansException() {
        final StaticApplicationContext context = new StaticApplicationContext();
        context.registerPrototype("objectId", ObjectId.class);
        final SpringImplementalAutowirer autowirer = new SpringImplementalAutowirer();
        autowirer.setApplicationContext(context);
        final Implemental implemental = new Implemental() {
            public void setObjectId(ObjectId id) {
                throw new UnsupportedOperationException("setObjectId");
            }
        };

        try {
            autowirer.autowire(implemental);
            fail("throw UnsatisfiedDependencyException");
        } catch (Throwable ex) {
            assertEquals(AutowireException.class, ex.getClass());
            assertNotNull(ex.getCause());
            assertEquals(BeanCreationException.class, ex.getCause().getClass());
            assertNotNull(ex.getCause().getCause());
            assertEquals(PropertyBatchUpdateException.class, ex.getCause().getCause().getClass());
            final PropertyAccessException pae = ((PropertyBatchUpdateException) ex.getCause().getCause())
                    .getPropertyAccessException("objectId");
            assertNotNull(pae);
            assertEquals(MethodInvocationException.class, pae.getClass());
            assertEquals(UnsupportedOperationException.class, pae.getCause().getClass());
            assertEquals("setObjectId", pae.getCause().getMessage());
        }
    }
}
