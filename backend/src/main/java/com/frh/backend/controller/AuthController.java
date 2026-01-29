package com.frh.backend.controller;

import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.dto.AuthResponse;
import com.frh.backend.dto.ErrorResponse;
import com.frh.backend.dto.LoginRequest;
import com.frh.backend.dto.RegisterRequest;
import com.frh.backend.service.AuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            HttpSession session) {

        AuthResponse response = authService.register(request);

        SupplierProfile supplier =
            authService.getSupplierByEmail(request.getEmail());

        session.setAttribute("user", supplier);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {

        AuthResponse response = authService.login(request, session);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {

        SupplierProfile supplier =
            (SupplierProfile) session.getAttribute("user");

        if (supplier == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Not logged in"));
        }

        return ResponseEntity.ok(supplier);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field =
                ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(
            new ErrorResponse(
                LocalDateTime.now(),
                400,
                "Validation failed",
                errors
            )
        );
    }
}
