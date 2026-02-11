package com.frh.backend.repository;

import com.frh.backend.model.Store;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
  List<Store> findBySupplierProfile_SupplierId(Long supplierId);

  // Find all active stores
  List<Store> findByIsActive(boolean isActive);
}
