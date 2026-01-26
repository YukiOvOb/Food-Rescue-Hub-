package com.frh.backend.Controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PeopleController {

    private final JdbcTemplate jdbc;

    public PeopleController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/people")
    public List<Map<String, Object>> people() {
        return jdbc.queryForList("SELECT id, name, age, gender FROM test ORDER BY id");
    }
}

