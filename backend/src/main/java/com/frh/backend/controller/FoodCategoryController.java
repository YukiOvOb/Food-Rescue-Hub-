package com.frh.backend.controller;

import com.frh.backend.model.FoodCategory;
import com.frh.backend.repository.FoodCategoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/food-categories")
public class FoodCategoryController {

  @Autowired private FoodCategoryRepository foodCategoryRepository;

  @GetMapping
  public ResponseEntity<List<FoodCategory>> getAllFoodCategories() {
    return ResponseEntity.ok(foodCategoryRepository.findAll());
  }
}
