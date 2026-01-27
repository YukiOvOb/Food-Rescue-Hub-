package com.frh.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.sql.*;
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

    // HelloController.java (Fixed: PreparedStatement, safe)
    @GetMapping("/hello-safe")
    public String helloSafe(@RequestParam String username) throws Exception {
        // Parameterized query using placeholders instead of string concatenation
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:testdb");
            PreparedStatement ps = connection.prepareStatement(sql)) {
            // Bind user input as a parameter, treating it as data rather than executable SQL
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String u = rs.getString("username");
                }
            }
        }
        return "ok";
    }

    //But more safely way is 
    // using Spring Data JPA repository methods that automatically parameterize queries:

    // public interface UserRepository extends JpaRepository<User, Long> {
    //     Optional<User> findByUsername(String username); 
    // }

}
