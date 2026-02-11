package com.frh.backend.controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;


import com.frh.backend.dto.AuthResponse;
import com.frh.backend.dto.ErrorResponse;
import com.frh.backend.dto.LoginRequest;
import com.frh.backend.dto.RegisterRequest;
import com.frh.backend.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<?> register(
      @Valid @RequestBody RegisterRequest request, HttpSession session) {

    AuthResponse response = authService.register(request);

    if ("CONSUMER".equalsIgnoreCase(request.getRole())) {
      session.setAttribute("USER_ID", response.getUserId());
      session.setAttribute("USER_ROLE", "CONSUMER");
      session.setAttribute("USER_EMAIL", response.getEmail());
      session.setAttribute("USER_DISPLAY_NAME", response.getDisplayName());
    } else {
      session.setAttribute("USER_ID", response.getUserId());
      session.setAttribute("USER_ROLE", "SUPPLIER");
      session.setAttribute("USER_EMAIL", response.getEmail());
      session.setAttribute("USER_DISPLAY_NAME", response.getDisplayName());
    }

    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpSession session) {

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

    Long userId = (Long) session.getAttribute("USER_ID");
    String role = (String) session.getAttribute("USER_ROLE");
    String email = (String) session.getAttribute("USER_EMAIL");
    String displayName = (String) session.getAttribute("USER_DISPLAY_NAME");

    if (userId == null || role == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "Not logged in"));
    }

    Map<String, Object> payload = new HashMap<>();
    payload.put("userId", userId);
    payload.put("id", userId); // backward compatibility for older UI code
    payload.put("role", role);
    payload.put("email", email);
    payload.put("displayName", displayName);
    // backward compatibility for existing supplier-facing code paths
    if ("SUPPLIER".equalsIgnoreCase(role)) {
      payload.put("supplierId", userId);
    }

    return ResponseEntity.ok(payload);
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpSession session) {
    session.invalidate();
    return ResponseEntity.ok(Map.of("message", "Logout successful"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String field = ((FieldError) error).getField();
              errors.put(field, error.getDefaultMessage());
            });

    return ResponseEntity.badRequest()
        .body(new ErrorResponse(LocalDateTime.now(), 400, "Validation failed", errors));
  }
}
