package com.brian.api.repository;

import com.brian.api.repository.dto.TestMemberAndDetailDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.brian.api.repository.entity.QTestMember.testMember;
import static com.brian.api.repository.entity.QTestMemberDetail.testMemberDetail;

@RequiredArgsConstructor
@Repository
public class TestQueryDslRepository {

    private final JPAQueryFactory factory;

    public List<TestMemberAndDetailDto> getMemberAndDetail(LocalDateTime searchStartDate, LocalDateTime searchEndDate) {
        List<TestMemberAndDetailDto> result = factory.select(
                        Projections.fields(
                                TestMemberAndDetailDto.class,
                                testMemberDetail.phone,
                                testMember.name,
                                testMember.id
                        ))
                .from(testMember)
                .innerJoin(testMemberDetail)
                .on(testMemberDetail.memberId.eq(testMember.id))
                .where(testMemberDetail.createDate.between(searchStartDate, searchEndDate))
                .fetch();

        return result;
    }
}
