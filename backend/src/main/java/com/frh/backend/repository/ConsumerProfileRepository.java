package com.frh.backend.repository;

import com.frh.backend.Model.ConsumerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerProfileRepository extends JpaRepository<ConsumerProfile, Long> {

}