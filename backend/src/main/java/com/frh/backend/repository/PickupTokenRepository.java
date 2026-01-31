package com.frh.backend.repository;

import com.frh.backend.Model.PickupToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PickupTokenRepository extends JpaRepository<PickupToken, Long> {

    Optional<PickupToken> findByOrderId(Long orderId);

    Optional<PickupToken> findByQrTokenHash(String qrTokenHash);
}
