package com.my.api.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@SpringBootTest
public class JdbcTest {

    @Autowired
    private TestEntityRepository repository;

    private static Connection connection;

    @Test
    void jdbc() throws Exception {
        connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/myschema?allowPublicKeyRetrieval=true&useSSL=false", "user", "password");
        String query = "insert into test_entity(name) values(?)";

//        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
        try (PreparedStatement statement = connection.prepareStatement(query, new String[] {"created_at","updatedAt"})) {
            statement.setString(1, "Foo");
            int affectedRows = statement.executeUpdate();

            assertThat(affectedRows).isEqualTo(1);

            ResultSet resultSet = statement.getResultSet();


            ResultSet keys = statement.getGeneratedKeys();
            keys.next();
            long pk = keys.getLong(1);

            assertThat(pk).isGreaterThan(0);
        } catch (SQLException e) {
        }
    }
}
