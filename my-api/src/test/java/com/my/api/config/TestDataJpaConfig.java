package com.my.api.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;

@TestConfiguration
public class TestDataJpaConfig {

    /**
     * DataJpaTest 를 사용하는 경우 JpaQueryFactory 빈을 생성하지 않기 때문에 직접 생성 필요
     * {@link org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest}
     * {@link com.my.api.base.BaseDataJpaTest}
     *
     * @param entityManager
     * @return
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
