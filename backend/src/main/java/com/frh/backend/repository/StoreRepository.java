package com.frh.backend.repository;

import com.frh.backend.Model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // Find all active stores
    List<Store> findByIsActive(boolean isActive);
}
