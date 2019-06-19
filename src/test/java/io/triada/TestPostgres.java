package io.triada;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;

public final class TestPostgres extends Assert{
    @ClassRule
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:9.6.2")
            .withUsername("almas")
            .withPassword("123")
            .withDatabaseName("hello");

    @Test
    public void testContainer() {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(container.getJdbcUrl());
        hikariConfig.setUsername(container.getUsername());
        hikariConfig.setPassword(container.getPassword());
        hikariConfig.setDriverClassName(container.getDriverClassName());

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(new HikariDataSource(hikariConfig));
        jdbcTemplate.execute("CREATE TABLE test(id SERIAL PRIMARY KEY,name TEXT);");
        jdbcTemplate.execute("INSERT INTO test (name) VALUES ('Almas')");
        final List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM test");
        assertTrue(!maps.isEmpty());
    }
}
