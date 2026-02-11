package com.frh.backend.repository;

import com.frh.backend.model.ConsumerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerProfileRepository extends JpaRepository<ConsumerProfile, Long> {

  /** Find consumer by email */
  Optional<ConsumerProfile> findByEmail(String email);

  /** Check if email exists */
  boolean existsByEmail(String email);

  // Find consumer by phone
  Optional<ConsumerProfile> findByPhone(String phone);
}
