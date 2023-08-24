package com.my.api.config.db;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ConditionalOnBean(SecondDataJpaConfig.class)
@Configuration
public class SecondQuerydslConfig {

    @PersistenceContext(unitName = "secondPersistenceUnit")
    private EntityManager entityManager;

    @Bean("secondJpaQueryFactory")
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
