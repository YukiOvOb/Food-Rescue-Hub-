package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Data
@Entity
@Table(name = "dietary_tags")
@Getter
@Setter
public class DietaryTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_name", unique = true)
    private String tagName;

    @ManyToMany(mappedBy = "dietaryTags")
    private List<Listing> listings;

    // 不要加 @Data，也不要重写 toString 包含 listings
}