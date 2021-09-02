package com.brian.api.service;

import com.brian.api.repository.TestEntityRepository;
import com.brian.api.repository.entity.TestEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class JdbcBatchTest {

    @Autowired
    TestEntityRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void jdbcBatchInsert() {
        List<TestEntity> users = getUsers();
        String sql = "INSERT INTO test_entity (name) values (?)";

        jdbcTemplate.batchUpdate(sql, users, 3, (ps, argument) -> ps.setString(1, argument.getName()));
    }

    private List<TestEntity> getUsers() {
        List<TestEntity> users = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            TestEntity user = TestEntity.builder()
                    .name("A"+i)
                    .build();

            users.add(user);
        }
        return users;
    }
}
