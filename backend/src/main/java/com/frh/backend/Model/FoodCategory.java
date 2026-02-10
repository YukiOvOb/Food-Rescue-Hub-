package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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
