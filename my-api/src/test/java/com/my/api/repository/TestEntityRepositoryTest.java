package com.my.api.repository;

import com.my.api.base.BaseDataJpaTest;
import com.my.api.repository.entity.TestEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@DataJpaTest
public class TestEntityRepositoryTest extends BaseDataJpaTest {

    @Autowired
    private TestEntityRepository repository;

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
}
