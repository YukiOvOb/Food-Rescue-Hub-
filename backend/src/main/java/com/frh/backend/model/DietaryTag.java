package com.frh.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "dietary_tags")
@Data
public class DietaryTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "tag_id", nullable = false)
  private Long tagId;

  @Column(name = "tag_name", nullable = false, unique = true, length = 60)
  private String tagName;
}
