package com.frh.backend.repository;

import com.frh.backend.Model.SupplierProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierProfileRepository
    extends JpaRepository<SupplierProfile, Long> {

  Optional<SupplierProfile> findByEmail(String email);

  Optional<SupplierProfile> findByPhone(String phone);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);
}
