package com.my.api.repository;

import com.my.api.repository.dto.TestDetailDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.my.api.repository.entity.QTestEntity.testEntity;
import static com.my.api.repository.entity.QTestEntity2.testEntity2;

@Repository
@RequiredArgsConstructor
public class TestEntityRepositoryImpl implements TestEntityQueryRepository{

    private final JPAQueryFactory factory;

    @Override
    public TestDetailDto findDetail(Long id) {
        return factory.select(
                Projections.bean(TestDetailDto.class,
                        testEntity.name,
                        testEntity2.phone,
                        testEntity2.address)
                )
                .from(testEntity)
                .leftJoin(testEntity2)
                    .on(testEntity2.name.eq(testEntity.name))
                .fetchJoin() // N+1 쿼리 방지
                .where(testEntity.id.eq(id))
                .fetchOne();
    }
}
