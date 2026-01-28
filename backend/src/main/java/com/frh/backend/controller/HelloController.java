package com.frh.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Spring Boot!");
    }

    @GetMapping("/login-safe-jdbc")
    public String loginSafeJdbc(@RequestParam String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:testdb");
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                // Do something meaningful (example: check if exists)
                if (rs.next()) {
                    return "user found";
                }
            }
        }

        return "user not found";
    }
}
