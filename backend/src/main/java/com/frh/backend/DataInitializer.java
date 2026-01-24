package com.frh.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public DataInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) throws Exception {
        // create table if not exists
        jdbc.execute("CREATE TABLE IF NOT EXISTS test (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), age INT, gender VARCHAR(32))");

        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM test", Integer.class);
        if (count == null || count == 0) {
            jdbc.update("INSERT INTO test (name, age, gender) VALUES (?, ?, ?)", "Alice Zhang", 28, "Female");
            jdbc.update("INSERT INTO test (name, age, gender) VALUES (?, ?, ?)", "Bob Li", 34, "Male");
            jdbc.update("INSERT INTO test (name, age, gender) VALUES (?, ?, ?)", "Carol Wang", 22, "Female");
            System.out.println("Inserted demo people into 'test' table (JDBC)");
        }
    }
}
