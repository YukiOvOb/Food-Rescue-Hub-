package com.frh.backend.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class ListingFoodCategoryId implements Serializable {

    @Column(name = "listing_id")
    private Long listingId;

    @Column(name = "category_id")
    private Long categoryId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListingFoodCategoryId that = (ListingFoodCategoryId) o;
        return Objects.equals(listingId, that.listingId)
                && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listingId, categoryId);
    }
}
