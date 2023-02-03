package io.github.dbstarll.dubai.model.spring.autoconfigure;

import io.github.dbstarll.dubai.model.service.ImplementalAutowirer;
import io.github.dbstarll.dubai.model.spring.ServiceBeanInitializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE,
        classes = {
                MongoAutoConfiguration.class,
                DatabaseAutoConfiguration.class,
                CollectionAutoConfiguration.class,
                ServiceAutoConfiguration.class
        })
public class ServiceAutoConfigurationTest implements ApplicationContextAware {
    private ApplicationContext ctx;

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
}