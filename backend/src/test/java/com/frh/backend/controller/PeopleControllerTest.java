package com.frh.backend.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.frh.backend.Model.TestPerson;
import com.frh.backend.repository.TestPersonRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PeopleControllerTest {

  @Autowired private PeopleController peopleController;

  @Autowired private TestPersonRepository testPersonRepository;

  @Test
  void peopleReturnsAllTestPersons() {
    // Add a test person to the repository to check person is existing
    TestPerson person = new TestPerson("John Doe", 30, "Male");
    testPersonRepository.save(person); // jpa persistance (cr)

    // retrieve the entire list of persons
    List<TestPerson> people = peopleController.findAllPeople(); // read

    boolean found = false;
    for (TestPerson p : people) {
      if ("John Doe".equals(p.getName()) && p.getAge() == 30 && "Male".equals(p.getGender())) {
        found = true;
      }
    }
    assertTrue(found);
  }
}
