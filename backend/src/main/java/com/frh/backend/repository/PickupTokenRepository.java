package com.frh.backend.repository;

import com.frh.backend.model.PickupToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PickupTokenRepository extends JpaRepository<PickupToken, Long> {

  Optional<PickupToken> findByOrderId(Long orderId);

  Optional<PickupToken> findByQrTokenHash(String qrTokenHash);
}
