package com.frh.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Spring Boot!");
    }

    @GetMapping("/insecure-login")
    public String insecureLogin(@RequestParam String username) throws SQLException {
        // Intentionally vulnerable SQL concatenation to trigger CodeQL SQL injection alert
        String query = "SELECT * FROM users WHERE username = '" + username + "'";
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:testdb");
             Statement statement = connection.createStatement()) {
            statement.executeQuery(query);
        }
        return "ok";
    }
}
