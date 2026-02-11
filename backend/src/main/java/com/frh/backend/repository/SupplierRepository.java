package com.frh.backend.repository;

import com.frh.backend.model.SupplierProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierProfile, Long> {}
