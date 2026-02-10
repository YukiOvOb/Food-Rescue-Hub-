package com.frh.backend.repository;

import com.frh.backend.Model.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodCategoryRepository extends JpaRepository<FoodCategory,Long> {

}
