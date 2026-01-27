package com.frh.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
<<<<<<< HEAD
        return Map.of("message", "Hello from Spring Boot!");
=======
        return Map.of("message", "Hello from Team08 Food rescue hub test on 27/1/26!");
>>>>>>> 51411f0 (First test)
    }
}
