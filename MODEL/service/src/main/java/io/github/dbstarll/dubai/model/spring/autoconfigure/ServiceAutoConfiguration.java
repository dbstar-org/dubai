package io.github.dbstarll.dubai.model.spring.autoconfigure;

import io.github.dbstarll.dubai.model.service.ImplementalAutowirer;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.spring.ServiceBeanInitializer;
import io.github.dbstarll.dubai.model.spring.SpringImplementalAutowirer;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(CollectionAutoConfiguration.class)
public class ServiceAutoConfiguration {
    /**
     * 遍历所有Service并为其装配ServiceBean，然后注入到Spring Context中.
     *
     * @return ServiceBeanInitializer实例.
     */
    @Bean
    @ConditionalOnMissingBean(ServiceBeanInitializer.class)
    BeanDefinitionRegistryPostProcessor serviceBeanInitializer() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(CollectionAutoConfiguration.loadBasePackages(Service.class,
                Service.class.getClassLoader()));
        return initializer;
    }

    /**
     * 注入一个ImplementalAutowirer实例，用于自动装配Service中的依赖项目.
     *
     * @return ImplementalAutowirer实例.
     */
    @Bean
    @ConditionalOnMissingBean(ImplementalAutowirer.class)
    ImplementalAutowirer implementalAutowirer() {
        return new SpringImplementalAutowirer();
    }
}
