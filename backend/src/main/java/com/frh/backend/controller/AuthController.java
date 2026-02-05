package com.frh.backend.controller;

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

        if ("CONSUMER".equalsIgnoreCase(request.getRole())) {
            var consumer = authService.getConsumerByEmail(request.getEmail());
            session.setAttribute("user", consumer);
            session.setAttribute("USER_ID", consumer.getConsumerId());
            session.setAttribute("USER_ROLE", "CONSUMER");
        } else {
            var supplier = authService.getSupplierByEmail(request.getEmail());
            session.setAttribute("user", supplier);
            session.setAttribute("USER_ID", supplier.getSupplierId());
            session.setAttribute("USER_ROLE", "SUPPLIER");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {

        // Support both consumer and supplier login
        AuthResponse response = authService.login(request, session);
        
        // retrieve info from session for console log
        Long userId = (Long) session.getAttribute("USER_ID");
        String userRole = (String) session.getAttribute("USER_ROLE");
        log.info("User logged in - ID: {}, Role: {}", userId, userRole);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {

        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Not logged in"));
        }

        return ResponseEntity.ok(user);
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
