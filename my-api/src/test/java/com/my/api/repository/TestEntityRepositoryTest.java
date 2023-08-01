package com.my.api.repository;

import com.my.api.base.BaseDataJpaTest;
import com.my.api.repository.dto.TestDetailDto;
import com.my.api.repository.entity.TestEntity;
import com.my.api.repository.entity.TestEntity2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@DataJpaTest
public class TestEntityRepositoryTest extends BaseDataJpaTest {

    @Autowired
    private TestEntityRepository repository;

    @Autowired
    private TestEntity2Repository repository2;

    @Test
    void save() {
        // given
        String name = "Brian";
        TestEntity entity = TestEntity.builder().name(name).build();

        // when
        repository.save(entity);

        // then
        assertNotNull(entity.getId());
        assertEquals(name, entity.getName());
        System.out.println(entity);
    }

    @Test
    void selectByQueryDsl() {
        // given
        String name = "Brian";
        TestEntity entity = TestEntity.builder()
                .name(name)
                .build();

        TestEntity2 entity2 = TestEntity2.builder()
                .name(name)
                .phone("010-1111-2222")
                .address("seoul, korea")
                .build();

        repository.save(entity);
        repository2.save(entity2);

        // when
        TestDetailDto detail = repository.findDetail(entity.getId());

        // then
        assertNotNull(detail);
        assertEquals(entity2.getPhone(), detail.getPhone());
    }
}
