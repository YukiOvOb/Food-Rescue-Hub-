package com.frh.backend.Model;

import jakarta.persistence.*;
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
