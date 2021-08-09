package io.github.dbstarll.dubai.model.spring.autoconfigure;

import io.github.dbstarll.dubai.model.service.ImplementalAutowirer;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.spring.ServiceBeanInitializer;
import io.github.dbstarll.dubai.model.spring.SpringImplementalAutowirer;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(CollectionAutoConfiguration.class)
public class ServiceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ServiceBeanInitializer.class)
    static BeanDefinitionRegistryPostProcessor serviceBeanInitializer() {
        ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(CollectionAutoConfiguration.loadBasePackages(Service.class,
                CollectionAutoConfiguration.class.getClassLoader()));
        initializer.setRecursion(true);
        return initializer;
    }

    @Bean
    @ConditionalOnMissingBean(ImplementalAutowirer.class)
    ImplementalAutowirer implementalAutowirer() {
        return new SpringImplementalAutowirer();
    }
}
