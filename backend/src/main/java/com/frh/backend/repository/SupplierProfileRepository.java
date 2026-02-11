package com.frh.backend.repository;

import com.frh.backend.model.SupplierProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierProfileRepository extends JpaRepository<SupplierProfile, Long> {

  Optional<SupplierProfile> findByEmail(String email);

  Optional<SupplierProfile> findByPhone(String phone);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);
}
