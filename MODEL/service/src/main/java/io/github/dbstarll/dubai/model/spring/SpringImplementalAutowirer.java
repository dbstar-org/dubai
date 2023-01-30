package io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.service.AutowireException;
import io.github.dbstarll.dubai.model.service.Implemental;
import io.github.dbstarll.dubai.model.service.ImplementalAutowirer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringImplementalAutowirer implements ImplementalAutowirer, ApplicationContextAware {
    private AutowireCapableBeanFactory factory;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.factory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    public <I extends Implemental> void autowire(final I implemental) throws AutowireException {
        if (factory != null) {
            try {
                factory.autowireBeanProperties(implemental, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
            } catch (BeansException ex) {
                throw new AutowireException(ex);
            }
        } else {
            throw new AutowireException("AutowireCapableBeanFactory not set.");
        }
    }
}
