package com.frh.backend.Repository;

import com.frh.backend.Model.TestPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestPersonRepository extends JpaRepository<TestPerson, Long> {

}
