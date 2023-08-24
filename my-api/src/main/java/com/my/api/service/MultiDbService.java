package com.my.api.service;

import com.my.api.repository.TestEntityRepository;
import com.my.api.repository.entity.TestEntity;
import com.my.api.repository2.SecondTestEntityRepository;
import com.my.api.repository2.entity.SecondTestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MultiDbService {

    private final TestEntityRepository repository;
    private final SecondTestEntityRepository secondRepository;

    /**
     * 트랜잭션 이름 설정이 없으면 Primary 설정된 트랜잭션 매니저가 사용된다.
     *
     * 아래의 코드는 2개의 DB 에 대한 접근이다.
     * 이 코드가 동작하지 않을 것이라 예상하였으나
     * 메서드에 트랜잭션 설정이 있으므로 기본 트랜잭션이 사용되며
     * second 엔티티 조회시에는 second DB 에 대한 새로운 트랜잭션이 사용된다.
     *
     * 기본 트랜잭션은 이 메서드의 이름으로 생성되지만
     * second 는 SimpleJpaRepository 의 트랜잭션 설정으로 열리므로
     * findAll 메서드 이름으로 생성된다.
     */
    @Transactional
    public void repositoryTest() {
        List<TestEntity> testEntityList = repository.findAll();
        List<SecondTestEntity> secondTestEntityList = secondRepository.findAll();

        System.out.println();
    }
}
