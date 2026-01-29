package com.frh.backend.repository;

import com.frh.backend.Model.ListingPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, Long> {
}