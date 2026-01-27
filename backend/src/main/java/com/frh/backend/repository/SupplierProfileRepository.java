package com.frh.backend.repository;

import com.frh.backend.Model.SupplierProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierProfileRepository extends JpaRepository<SupplierProfile,Long> {
}
