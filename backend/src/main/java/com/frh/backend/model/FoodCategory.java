package com.frh.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "food_categories")
@Getter
@Setter
public class FoodCategory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name; // e.g., "Beef", "Poultry", "Vegetables"

  @Column(precision = 10, scale = 2)
  private BigDecimal kgCo2PerKg; // CO2 equivalent per kg of food
}
