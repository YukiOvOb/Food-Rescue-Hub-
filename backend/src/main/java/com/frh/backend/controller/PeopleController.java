package com.frh.backend.controller;

import com.frh.backend.model.TestPerson;
import com.frh.backend.repository.TestPersonRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PeopleController {

  @Autowired private TestPersonRepository testPersonRepository;

  @GetMapping("/people")
  public List<TestPerson> findAllPeople() {
    return testPersonRepository.findAll();
  }
}
