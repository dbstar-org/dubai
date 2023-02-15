package io.github.dbstarll.dubai.model.spring.autoconfigure;

import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.service.ImplementalAutowirer;
import io.github.dbstarll.dubai.model.service.test.InterfaceService;
import io.github.dbstarll.dubai.model.spring.ServiceBeanInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest(webEnvironment = WebEnvironment.NONE,
        classes = {
                MongoAutoConfiguration.class,
                DatabaseAutoConfiguration.class,
                CollectionAutoConfiguration.class,
                ServiceAutoConfiguration.class
        })
public class ServiceAutoConfigurationTest implements ApplicationContextAware {
    private ApplicationContext ctx;

    @Autowired(required = false)
    private InterfaceService service1;

    @Autowired(required = false)
    private io.github.dbstarll.dubai.model.service.test2.InterfaceService service2;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }

    @Test
    public void serviceBeanInitializer() {
        assertNotNull(ctx.getBean("serviceBeanInitializer", ServiceBeanInitializer.class));
    }

    @Test
    public void implementalAutowirer() {
        assertNotNull(ctx.getBean("implementalAutowirer", ImplementalAutowirer.class));
        for (String name : ctx.getBeanDefinitionNames()) {
            System.out.println(name);
        }
    }

    @Test
    public void interfaceService() {
        assertNotNull(service1);
        assertSame(InterfaceEntity.class, service1.getEntityClass());
    }

    @Test
    public void interfaceService2() {
        assertNull(service2);
    }
}