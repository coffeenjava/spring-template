package com.my.api.controller;

import com.my.api.common.consts.YesNo;
import com.my.api.repository.TestEntity2Repository;
import com.my.api.repository.TestEntityRepository;
import com.my.api.repository.entity.TestEntity;
import com.my.api.repository.entity.TestEntity2;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TestService {

    private final TestEntityRepository repository;
    private final TestEntity2Repository repository2;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void test(String name) {
        TestEntity e = TestEntity.builder()
                .name(name)
                .adultYn(YesNo.YES)
                .build();

        repository.save(e); // insert 수행
        Assert.isNull(e.getCreatedAt(), "생성일은 null");

        TestEntity e2 = repository.findById(e.getId()).get(); // select 수행되지 않음
        Assert.isTrue(e2 == e, "엔티티매니저에서 가져왔으므로 주소까지 동일한 엔티티");

//        em.refresh(e);

        List<TestEntity> list = repository.findAll(); // select 수행
        Assert.notEmpty(list, "select 수행으로 가져온 목록이 존재");
        Optional<TestEntity> maybeE3 = list.stream().filter(item -> item == e).findFirst();
        Assert.isTrue(maybeE3.isPresent(), "엔티티매니저에 e 엔티티가 이미 존재하므로 새로 조회된 엔티티가 아닌 e 엔티티가 담겨있다");
        TestEntity e3 = maybeE3.get();
        Assert.isNull(e3.getCreatedAt(), "여전히 생성일 null");

        Assert.isTrue(e == e2 && e == e3, "3개의 엔티티는 모두 동일");

        e.setAdultYn(YesNo.NO); // 엔티티 값 변경. update 수행되지 않음

        TestEntity e4 = repository.findById(e.getId()).get(); // select 수행되지 않음
        Assert.isTrue(e == e4, "엔티티매니저에서 가져왔으므로 주소까지 동일한 엔티티");

        TestEntity2 te = TestEntity2.builder()
                .phone("000-0000-0000")
                .build();

        repository2.save(te);

        te.setPhone("111-1111-1111");

        List<TestEntity> list2 = repository.findAll(); // 변경된 엔티티값 update 수행 후 select 수행
        System.out.println();
    }
}
