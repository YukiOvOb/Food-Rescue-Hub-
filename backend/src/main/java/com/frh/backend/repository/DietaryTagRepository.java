package com.frh.backend.repository;

import com.frh.backend.Model.DietaryTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DietaryTagRepository extends JpaRepository<DietaryTag, Long> {
    Optional<DietaryTag> findByTagName(String tagName);
}