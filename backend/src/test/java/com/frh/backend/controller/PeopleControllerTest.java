package com.frh.backend.controller;

import com.frh.backend.Model.TestPerson;
import com.frh.backend.repository.TestPersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PeopleControllerTest {

    @Autowired
    private PeopleController peopleController;

    @Autowired
    private TestPersonRepository testPersonRepository;

    @Test
    void peopleReturnsAllTestPersons() {
        // Add a test person to the repository to check person is existing
        TestPerson person = new TestPerson("John Doe", 30, "Male");
        testPersonRepository.save(person); //jpa persistance (cr)

        // retrieve the entire list of persons
        List<TestPerson> people = peopleController.findAllPeople(); //read

        boolean found = false;
        for (TestPerson p : people) {
            if ("John Doe".equals(p.getName()) && p.getAge() == 30 && "Male".equals(p.getGender())) {
                found = true;
            }
        }
        assertTrue(found);
    }
}
