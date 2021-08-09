package io.github.dbstarll.dubai.model.entity.utils;

import org.apache.commons.beanutils.BeanIntrospector;
import org.apache.commons.beanutils.DefaultBeanIntrospector;
import org.apache.commons.beanutils.IntrospectionContext;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.IntrospectionException;

class EntityPropertyUtilsBean extends PropertyUtilsBean {
    static final EntityPropertyUtilsBean instance = new EntityPropertyUtilsBean();

    private EntityPropertyUtilsBean() {
        addBeanIntrospector(new InterfaceBeanIntrospector());
    }

    private static class InterfaceBeanIntrospector implements BeanIntrospector {
        @Override
        public void introspect(IntrospectionContext icontext) throws IntrospectionException {
            if (icontext.getTargetClass().isInterface()) {
                introspect(icontext, icontext.getTargetClass(), true);
            }
        }

        private void introspect(IntrospectionContext icontext, Class<?> interfaceClass, boolean root)
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