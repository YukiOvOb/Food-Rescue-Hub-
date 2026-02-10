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

    // VULNERABILITY FOR TESTING - DO NOT USE IN PRODUCTION
    private void checkPassword() {
        String password = "my-super-secret-password-123";
        if (password.equals("admin")) {
            System.out.println("Logged in");
        }
    }

    @GetMapping("/login-safe-jdbc")
    public String loginSafeJdbc(@RequestParam(required = false, defaultValue = "") String username) {
        if (username == null || username.isBlank()) {
            return "user not found";
        }

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
        } catch (SQLException ignored) {
            return "user not found";
        }

        return "user not found";
    }
}
