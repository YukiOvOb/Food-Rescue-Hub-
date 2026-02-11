package com.frh.backend.repository;

import com.frh.backend.model.StoreType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreTypeRepository extends JpaRepository<StoreType, Long> {

  // Find store type by type name
  Optional<StoreType> findByTypeName(String typeName);
}
