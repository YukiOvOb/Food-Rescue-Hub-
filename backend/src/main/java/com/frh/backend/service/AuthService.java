package com.frh.backend.service;

import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.dto.AuthResponse;
import com.frh.backend.dto.LoginRequest;
import com.frh.backend.dto.RegisterRequest;
import com.frh.backend.repository.ConsumerProfileRepository;
import com.frh.backend.repository.SupplierProfileRepository;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
                "Registration successful"
            );
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
            "Registration successful"
        );
    }

    public AuthResponse login(LoginRequest req, HttpSession session) {
        
        // Try consumer login first
        Optional<ConsumerProfile> consumerOpt = consumerRepo.findByEmail(req.getEmail());
        if (consumerOpt.isPresent()) {
            ConsumerProfile consumer = consumerOpt.get();
            
            if (!encoder.matches(req.getPassword(), consumer.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }
            
            // Set session attributes for consumer
            session.setAttribute("user", consumer);
            session.setAttribute("USER_ID", consumer.getConsumerId());
            session.setAttribute("USER_ROLE", "CONSUMER");
            
            log.info("Consumer logged in: {} (ID: {})", consumer.getEmail(), consumer.getConsumerId());
            
            return new AuthResponse(
                null,
                consumer.getConsumerId(),
                consumer.getEmail(),
                consumer.getDisplayName(),
                "CONSUMER",
                "Login successful"
            );
        }
        
        // Try supplier login
        Optional<SupplierProfile> supplierOpt = supplierRepo.findByEmail(req.getEmail());
        if (supplierOpt.isPresent()) {
            SupplierProfile supplier = supplierOpt.get();
            
            if (!encoder.matches(req.getPassword(), supplier.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }
            
            // Set session attributes for supplier
            session.setAttribute("user", supplier);
            session.setAttribute("USER_ID", supplier.getSupplierId());
            session.setAttribute("USER_ROLE", "SUPPLIER");
            
            log.info("Supplier logged in: {} (ID: {})", supplier.getEmail(), supplier.getSupplierId());
            
            return new AuthResponse(
                null,
                supplier.getSupplierId(),
                supplier.getEmail(),
                supplier.getDisplayName(),
                "SUPPLIER",
                "Login successful"
            );
        }
        
        throw new RuntimeException("Invalid email or password");
    }

    public SupplierProfile getSupplierByEmail(String email) {
        return supplierRepo.findByEmail(email)
            .orElseThrow();
    }
    
    public ConsumerProfile getConsumerByEmail(String email) {
        return consumerRepo.findByEmail(email)
            .orElseThrow();
    }
}
