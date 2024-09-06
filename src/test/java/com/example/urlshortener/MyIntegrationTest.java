package com.example.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@Testcontainers
public class MyIntegrationTest {


    @Container
    public static PostgreSQLContainer<?> postgresDB = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDB::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDB::getUsername);
        registry.add("spring.datasource.password", postgresDB::getPassword);
    }

    @Test
    void contextLoads() {

        System.out.println("PostgreSQL Container is running: " + postgresDB.isRunning());
        System.out.println("PostgreSQL JDBC URL: " + postgresDB.getJdbcUrl());
        assertTrue(postgresDB.isRunning());
        assertNotNull(postgresDB.getJdbcUrl());

        try (Connection connection = DriverManager.getConnection(
                postgresDB.getJdbcUrl(),
                postgresDB.getUsername(),
                postgresDB.getPassword()
        )) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        } catch (SQLException e) {
            fail("Failed to connect to the database: " + e.getMessage());
        }
    }
}
