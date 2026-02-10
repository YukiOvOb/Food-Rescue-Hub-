package com.frh.backend.controller;

import com.frh.backend.Model.FoodCategory;
import com.frh.backend.repository.FoodCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/food-categories")
public class FoodCategoryController {

    @Autowired
    private FoodCategoryRepository foodCategoryRepository;

    @GetMapping
    public ResponseEntity<List<FoodCategory>> getAllFoodCategories() {
        return ResponseEntity.ok(foodCategoryRepository.findAll());
    }
}
