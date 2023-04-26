package io.github.dbstarll.dubai.model.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public final class BeanMapImplementalAutowirer implements ImplementalAutowirer {
    private final BeanMap beanMap;

    public BeanMapImplementalAutowirer(BeanMap beanMap) {
        this.beanMap = beanMap;
    }

    @Override
    public <I extends Implemental> void autowire(I implemental) throws AutowireException {
        Arrays.stream(implemental.getClass().getMethods())
                .filter(m -> m.getName().startsWith("set"))
                .filter(m -> m.getReturnType() == Void.TYPE)
                .filter(m -> m.getParameterCount() == 1)
                .filter(m -> Service.class.isAssignableFrom(m.getParameterTypes()[0]))
                .forEach(m -> {
                    try {
                        m.invoke(implemental, beanMap.get(m.getParameterTypes()[0]));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new AutowireException(e);
                    }
                });
    }
}
