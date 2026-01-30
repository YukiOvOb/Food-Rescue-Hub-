package com.frh.backend.service;

import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.dto.AuthResponse;
import com.frh.backend.dto.LoginRequest;
import com.frh.backend.dto.RegisterRequest;
import com.frh.backend.repository.SupplierProfileRepository;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final SupplierProfileRepository repo;
    private final PasswordEncoder encoder;

    @Transactional
    public AuthResponse register(RegisterRequest req) {

        if (repo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (req.getPhone() != null &&
            repo.existsByPhone(req.getPhone())) {
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

        repo.save(s);

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

        SupplierProfile s = repo.findByEmail(req.getEmail())
            .orElseThrow(() ->
                new RuntimeException("Invalid email or password"));

        if (!encoder.matches(req.getPassword(), s.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        session.setAttribute("user", s);

        return new AuthResponse(
            null,
            s.getSupplierId(),
            s.getEmail(),
            s.getDisplayName(),
            s.getRole(),
            "Login successful"
        );
    }

    public SupplierProfile getSupplierByEmail(String email) {
        return repo.findByEmail(email)
            .orElseThrow();
    }
}
