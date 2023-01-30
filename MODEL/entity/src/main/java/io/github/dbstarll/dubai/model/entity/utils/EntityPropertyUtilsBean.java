package io.github.dbstarll.dubai.model.entity.utils;

import org.apache.commons.beanutils.BeanIntrospector;
import org.apache.commons.beanutils.DefaultBeanIntrospector;
import org.apache.commons.beanutils.IntrospectionContext;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.IntrospectionException;

final class EntityPropertyUtilsBean extends PropertyUtilsBean {
    static final EntityPropertyUtilsBean INSTANCE = new EntityPropertyUtilsBean();

    private EntityPropertyUtilsBean() {
        addBeanIntrospector(new InterfaceBeanIntrospector());
    }

    private static class InterfaceBeanIntrospector implements BeanIntrospector {
        @Override
        public void introspect(final IntrospectionContext icontext) throws IntrospectionException {
            if (icontext.getTargetClass().isInterface()) {
                introspect(icontext, icontext.getTargetClass(), true);
            }
        }

        private void introspect(final IntrospectionContext icontext, final Class<?> interfaceClass, final boolean root)
                throws IntrospectionException {
            if (!root) {
                DefaultBeanIntrospector.INSTANCE.introspect(new IntrospectionContextWrapper(icontext, interfaceClass));
            }

            for (Class<?> superInterface : interfaceClass.getInterfaces()) {
                introspect(icontext, superInterface, false);
            }
        }
    }
}
