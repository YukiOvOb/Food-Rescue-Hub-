package com.frh.backend.repository;

import com.frh.backend.Model.StoreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreTypeRepository extends JpaRepository<StoreType, Long> {

    // Find store type by type name
    Optional<StoreType> findByTypeName(String typeName);
}
