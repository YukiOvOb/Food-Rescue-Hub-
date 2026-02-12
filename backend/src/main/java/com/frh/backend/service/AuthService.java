package com.frh.backend.service;

import com.frh.backend.dto.AuthResponse;
import com.frh.backend.dto.LoginRequest;
import com.frh.backend.dto.RegisterRequest;
import com.frh.backend.model.ConsumerProfile;
import com.frh.backend.model.SupplierProfile;
import com.frh.backend.repository.ConsumerProfileRepository;
import com.frh.backend.repository.SupplierProfileRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final SupplierProfileRepository supplierRepo;
  private final ConsumerProfileRepository consumerRepo;
  private final PasswordEncoder encoder;

  @Transactional
  public AuthResponse register(RegisterRequest req) {
    String role = req.getRole(); // expected: "CONSUMER" or "SUPPLIER"
    if ("CONSUMER".equalsIgnoreCase(role)) {
      if (consumerRepo.findByEmail(req.getEmail()).isPresent()) {
        throw new RuntimeException("Email already registered");
      }
      if (req.getPhone() != null && consumerRepo.findByPhone(req.getPhone()).isPresent()) {
        throw new RuntimeException("Phone already registered");
      }

      ConsumerProfile c = new ConsumerProfile();
      c.setEmail(req.getEmail());
      c.setPassword(encoder.encode(req.getPassword()));
      c.setPhone(req.getPhone());
      c.setDisplayName(req.getDisplayName());
      c.setStatus("ACTIVE");
      c.setRole("CONSUMER");

      consumerRepo.save(c);

      return new AuthResponse(
          null,
          c.getConsumerId(),
          c.getEmail(),
          c.getDisplayName(),
          c.getRole(),
          "Registration successful");
    }

    // default to supplier
    if (supplierRepo.existsByEmail(req.getEmail())) {
      throw new RuntimeException("Email already registered");
    }
    if (req.getPhone() != null && supplierRepo.existsByPhone(req.getPhone())) {
      throw new RuntimeException("Phone already registered");
    }

    SupplierProfile s = new SupplierProfile();
    s.setEmail(req.getEmail());
    s.setPassword(encoder.encode(req.getPassword()));
    s.setPhone(req.getPhone());
    s.setDisplayName(req.getDisplayName());
    s.setBusinessName(req.getBusinessName());
    s.setBusinessType(req.getBusinessType());
    s.setPayoutAccountRef(req.getPayoutAccountRef());
    s.setStatus("ACTIVE");
    s.setRole("SUPPLIER");

    supplierRepo.save(s);

    return new AuthResponse(
        null,
        s.getSupplierId(),
        s.getEmail(),
        s.getDisplayName(),
        s.getRole(),
        "Registration successful");
  }

  public AuthResponse login(LoginRequest req, HttpSession session) {
    String requestedRole = req.getRole() == null ? "" : req.getRole().trim().toUpperCase(Locale.ROOT);

    if ("CONSUMER".equals(requestedRole)) {
      ConsumerProfile consumer =
          consumerRepo
              .findByEmail(req.getEmail())
              .orElseThrow(() -> new RuntimeException("Invalid email or password"));

      // Check password with support for both BCrypt and plain-text passwords
      if (!verifyAndUpdatePassword(consumer.getPassword(), req.getPassword())) {
        throw new RuntimeException("Invalid email or password");
      }

      // If password was plain-text, encode and save it
      if (!isBcryptEncoded(consumer.getPassword())) {
        consumer.setPassword(encoder.encode(req.getPassword()));
        consumerRepo.save(consumer);
        log.info("Consumer password upgraded to BCrypt: {}", consumer.getEmail());
      }

      // Store only scalar session data; avoid persisting JPA entities in JDBC session
      session.setAttribute("USER_ID", consumer.getConsumerId());
      session.setAttribute("USER_ROLE", "CONSUMER");
      session.setAttribute("USER_EMAIL", consumer.getEmail());
      session.setAttribute("USER_DISPLAY_NAME", consumer.getDisplayName());

      log.info("Consumer logged in: {} (ID: {})", consumer.getEmail(), consumer.getConsumerId());

      return new AuthResponse(
          null,
          consumer.getConsumerId(),
          consumer.getEmail(),
          consumer.getDisplayName(),
          "CONSUMER",
          "Login successful");
    }

    if ("SUPPLIER".equals(requestedRole)) {
      SupplierProfile supplier =
          supplierRepo
              .findByEmail(req.getEmail())
              .orElseThrow(() -> new RuntimeException("Invalid email or password"));

      // Check password with support for both BCrypt and plain-text passwords
      if (!verifyAndUpdatePassword(supplier.getPassword(), req.getPassword())) {
        throw new RuntimeException("Invalid email or password");
      }

      // If password was plain-text, encode and save it
      if (!isBcryptEncoded(supplier.getPassword())) {
        supplier.setPassword(encoder.encode(req.getPassword()));
        supplierRepo.save(supplier);
        log.info("Supplier password upgraded to BCrypt: {}", supplier.getEmail());
      }

      // Store only scalar session data; avoid persisting JPA entities in JDBC session
      session.setAttribute("USER_ID", supplier.getSupplierId());
      session.setAttribute("USER_ROLE", "SUPPLIER");
      session.setAttribute("USER_EMAIL", supplier.getEmail());
      session.setAttribute("USER_DISPLAY_NAME", supplier.getDisplayName());

      log.info("Supplier logged in: {} (ID: {})", supplier.getEmail(), supplier.getSupplierId());

      return new AuthResponse(
          null,
          supplier.getSupplierId(),
          supplier.getEmail(),
          supplier.getDisplayName(),
          "SUPPLIER",
          "Login successful");
    }

    throw new RuntimeException("Unauthorized role for login");
  }

  /**
   * Verify password against a stored password hash. Supports both BCrypt-encoded passwords and
   * plain-text passwords (for backward compatibility).
   */
  private boolean verifyAndUpdatePassword(String storedPassword, String plainPassword) {
    // First try BCrypt comparison (for already encoded passwords)
    if (encoder.matches(plainPassword, storedPassword)) {
      return true;
    }

    // Fall back to plain-text comparison (for non-encoded passwords)
    if (plainPassword.equals(storedPassword)) {
      return true;
    }

    return false;
  }

  /** Check if a password is BCrypt-encoded. BCrypt hashes start with "$2a$", "$2b$", or "$2y$" */
  private boolean isBcryptEncoded(String password) {
    return password != null
        && (password.startsWith("$2a$")
            || password.startsWith("$2b$")
            || password.startsWith("$2y$"));
  }

  public SupplierProfile getSupplierByEmail(String email) {
    return supplierRepo.findByEmail(email).orElseThrow();
  }

  public ConsumerProfile getConsumerByEmail(String email) {
    return consumerRepo.findByEmail(email).orElseThrow();
  }
}
