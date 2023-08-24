package com.my.api.config.db;

import com.my.api.Application;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
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

@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = DataJpaConfig.ENTITY_PACKAGE
)
@Configuration
public class DataJpaConfig {

    public static final String ENTITY_PACKAGE = Application.BASE_PACKAGE_MYAPP + ".repository";

    /**
     * 기본 JPAQueryFactory 생성에 사용할 수 있도록 Primary 설정 필수
     */
    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource,
            ConfigurableListableBeanFactory beanFactory
    ) {
        Map<String, String> properties = new HashMap<>();
        LocalContainerEntityManagerFactoryBean emfb = builder
                .dataSource(dataSource)
                .properties(properties)
                .packages(ENTITY_PACKAGE)
                .build();

        /**
         * 참고: {@link SpringBeanContainer}
         */
        emfb.getJpaPropertyMap().put(AvailableSettings.BEAN_CONTAINER, new SpringBeanContainer(beanFactory));
        return emfb;
    }

    /**
     * 기본 TransactionManager 를 사용할 수 있도록 하기 위해 Primary 설정
     * 설정하지 않으면 @Transactional 사용시 이름을 명시해야 한다.
     */
    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
