package com.frh.backend.repository;

import com.frh.backend.Model.DietaryTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DietaryTagRepository extends JpaRepository<DietaryTag, Long> {
}