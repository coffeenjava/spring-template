package com.my.api.config.db;

import com.my.api.Application;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnBean(SecondDataSourceConfig.class)
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = SecondDataJpaConfig.ENTITY_PACKAGE,
        entityManagerFactoryRef = "secondEntityManagerFactory",
        transactionManagerRef = "secondTransactionManager"
)
@Configuration
public class SecondDataJpaConfig {

    public static final String ENTITY_PACKAGE = Application.BASE_PACKAGE_MYAPP + ".repository2";

    @Bean("secondEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("secondDataSource") DataSource dataSource,
            ConfigurableListableBeanFactory beanFactory
    ) {
        Map<String, String> properties = new HashMap<>();
        LocalContainerEntityManagerFactoryBean emfb = builder
                .dataSource(dataSource)
                .properties(properties)
                .packages(ENTITY_PACKAGE)
                .persistenceUnit("secondPersistenceUnit")
                .build();

        /**
         * 참고: {@link SpringBeanContainer}
         */
        emfb.getJpaPropertyMap().put(AvailableSettings.BEAN_CONTAINER, new SpringBeanContainer(beanFactory));
        return emfb;
    }

    @Bean("secondTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("secondEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
