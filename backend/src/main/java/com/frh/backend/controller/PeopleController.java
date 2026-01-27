package com.frh.backend.controller;

import com.frh.backend.Model.TestPerson;
import com.frh.backend.repository.TestPersonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PeopleController {

    @Autowired
    private TestPersonRepository testPersonRepository;

    @GetMapping("/people")
    public List<TestPerson> findAllPeople() {
        return testPersonRepository.findAll();
    }
}

