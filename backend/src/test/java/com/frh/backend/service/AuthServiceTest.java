package com.frh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.dto.AuthResponse;
import com.frh.backend.dto.LoginRequest;
import com.frh.backend.dto.RegisterRequest;
import com.frh.backend.repository.ConsumerProfileRepository;
import com.frh.backend.repository.SupplierProfileRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private SupplierProfileRepository supplierRepo;

  @Mock private ConsumerProfileRepository consumerRepo;

  @Mock private PasswordEncoder encoder;

  @InjectMocks private AuthService authService;

  @Test
  void registerConsumer_success() {
    RegisterRequest request = new RegisterRequest();
    request.setRole("CONSUMER");
    request.setEmail("consumer@test.com");
    request.setPassword("Password@123");
    request.setPhone("90000001");
    request.setDisplayName("Consumer One");

    when(consumerRepo.findByEmail("consumer@test.com")).thenReturn(Optional.empty());
    when(consumerRepo.findByPhone("90000001")).thenReturn(Optional.empty());
    when(encoder.encode("Password@123")).thenReturn("ENCODED");
    when(consumerRepo.save(any(ConsumerProfile.class)))
        .thenAnswer(
            invocation -> {
              ConsumerProfile c = invocation.getArgument(0);
              c.setConsumerId(101L);
              return c;
            });

    AuthResponse response = authService.register(request);

    assertEquals(101L, response.getUserId());
    assertEquals("consumer@test.com", response.getEmail());
    assertEquals("CONSUMER", response.getRole());
    assertEquals("Registration successful", response.getMessage());
  }

  @Test
  void registerConsumer_duplicateEmail_throws() {
    RegisterRequest request = new RegisterRequest();
    request.setRole("CONSUMER");
    request.setEmail("consumer@test.com");

    when(consumerRepo.findByEmail("consumer@test.com"))
        .thenReturn(Optional.of(new ConsumerProfile()));

    RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
    assertEquals("Email already registered", ex.getMessage());
  }

  @Test
  void registerConsumer_duplicatePhone_throws() {
    RegisterRequest request = new RegisterRequest();
    request.setRole("CONSUMER");
    request.setEmail("consumer@test.com");
    request.setPhone("90000001");

    when(consumerRepo.findByEmail("consumer@test.com")).thenReturn(Optional.empty());
    when(consumerRepo.findByPhone("90000001")).thenReturn(Optional.of(new ConsumerProfile()));

    RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
    assertEquals("Phone already registered", ex.getMessage());
  }

  @Test
  void registerSupplier_success() {
    RegisterRequest request = new RegisterRequest();
    request.setRole("SUPPLIER");
    request.setEmail("supplier@test.com");
    request.setPassword("Password@123");
    request.setPhone("80000001");
    request.setDisplayName("Supplier One");
    request.setBusinessName("Test Bakery");
    request.setBusinessType("F&B");
    request.setPayoutAccountRef("acct_123");

    when(supplierRepo.existsByEmail("supplier@test.com")).thenReturn(false);
    when(supplierRepo.existsByPhone("80000001")).thenReturn(false);
    when(encoder.encode("Password@123")).thenReturn("ENCODED");
    when(supplierRepo.save(any(SupplierProfile.class)))
        .thenAnswer(
            invocation -> {
              SupplierProfile s = invocation.getArgument(0);
              s.setSupplierId(202L);
              return s;
            });

    AuthResponse response = authService.register(request);

    assertEquals(202L, response.getUserId());
    assertEquals("supplier@test.com", response.getEmail());
    assertEquals("SUPPLIER", response.getRole());
    assertEquals("Registration successful", response.getMessage());
  }

  @Test
  void registerSupplier_duplicateEmail_throws() {
    RegisterRequest request = new RegisterRequest();
    request.setRole("SUPPLIER");
    request.setEmail("supplier@test.com");

    when(supplierRepo.existsByEmail("supplier@test.com")).thenReturn(true);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
    assertEquals("Email already registered", ex.getMessage());
  }

  @Test
  void loginConsumer_withBcryptPassword_successWithoutUpgrade() {
    LoginRequest request = new LoginRequest();
    request.setEmail("consumer@test.com");
    request.setPassword("Password@123");
    MockHttpSession session = new MockHttpSession();

    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(11L);
    consumer.setEmail("consumer@test.com");
    consumer.setDisplayName("Consumer One");
    consumer.setPassword("$2a$bcryptHash");

    when(consumerRepo.findByEmail("consumer@test.com")).thenReturn(Optional.of(consumer));
    when(encoder.matches("Password@123", "$2a$bcryptHash")).thenReturn(true);

    AuthResponse response = authService.login(request, session);

    assertEquals(11L, response.getUserId());
    assertEquals("CONSUMER", response.getRole());
    assertEquals(11L, session.getAttribute("USER_ID"));
    assertEquals("CONSUMER", session.getAttribute("USER_ROLE"));
    verify(consumerRepo, never()).save(any(ConsumerProfile.class));
  }

  @Test
  void loginConsumer_plainTextPassword_upgradesHash() {
    LoginRequest request = new LoginRequest();
    request.setEmail("consumer@test.com");
    request.setPassword("plain-pass");
    HttpSession session = new MockHttpSession();

    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(12L);
    consumer.setEmail("consumer@test.com");
    consumer.setDisplayName("Consumer Two");
    consumer.setPassword("plain-pass");

    when(consumerRepo.findByEmail("consumer@test.com")).thenReturn(Optional.of(consumer));
    when(encoder.matches("plain-pass", "plain-pass")).thenReturn(false);
    when(encoder.encode("plain-pass")).thenReturn("$2a$upgraded");
    when(consumerRepo.save(any(ConsumerProfile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AuthResponse response = authService.login(request, session);

    assertEquals("CONSUMER", response.getRole());
    ArgumentCaptor<ConsumerProfile> captor = ArgumentCaptor.forClass(ConsumerProfile.class);
    verify(consumerRepo).save(captor.capture());
    assertEquals("$2a$upgraded", captor.getValue().getPassword());
  }

  @Test
  void loginSupplier_success() {
    LoginRequest request = new LoginRequest();
    request.setEmail("supplier@test.com");
    request.setPassword("Password@123");
    MockHttpSession session = new MockHttpSession();

    SupplierProfile supplier = new SupplierProfile();
    supplier.setSupplierId(21L);
    supplier.setEmail("supplier@test.com");
    supplier.setDisplayName("Supplier One");
    supplier.setPassword("$2b$bcryptHash");

    when(consumerRepo.findByEmail("supplier@test.com")).thenReturn(Optional.empty());
    when(supplierRepo.findByEmail("supplier@test.com")).thenReturn(Optional.of(supplier));
    when(encoder.matches("Password@123", "$2b$bcryptHash")).thenReturn(true);

    AuthResponse response = authService.login(request, session);

    assertEquals(21L, response.getUserId());
    assertEquals("SUPPLIER", response.getRole());
    assertEquals(21L, session.getAttribute("USER_ID"));
    assertEquals("SUPPLIER", session.getAttribute("USER_ROLE"));
  }

  @Test
  void login_invalidCredentials_throws() {
    LoginRequest request = new LoginRequest();
    request.setEmail("missing@test.com");
    request.setPassword("wrong");

    when(consumerRepo.findByEmail("missing@test.com")).thenReturn(Optional.empty());
    when(supplierRepo.findByEmail("missing@test.com")).thenReturn(Optional.empty());

    RuntimeException ex =
        assertThrows(
            RuntimeException.class, () -> authService.login(request, new MockHttpSession()));

    assertEquals("Invalid email or password", ex.getMessage());
  }

  @Test
  void login_consumerWrongPassword_throws() {
    LoginRequest request = new LoginRequest();
    request.setEmail("consumer@test.com");
    request.setPassword("wrong");

    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setEmail("consumer@test.com");
    consumer.setPassword("stored");

    when(consumerRepo.findByEmail("consumer@test.com")).thenReturn(Optional.of(consumer));
    when(encoder.matches("wrong", "stored")).thenReturn(false);

    RuntimeException ex =
        assertThrows(
            RuntimeException.class, () -> authService.login(request, new MockHttpSession()));

    assertEquals("Invalid email or password", ex.getMessage());
  }

  @Test
  void getSupplierByEmail_foundAndMissing() {
    SupplierProfile supplier = new SupplierProfile();
    supplier.setSupplierId(200L);

    when(supplierRepo.findByEmail("supplier@test.com")).thenReturn(Optional.of(supplier));
    assertEquals(200L, authService.getSupplierByEmail("supplier@test.com").getSupplierId());

    when(supplierRepo.findByEmail("missing@test.com")).thenReturn(Optional.empty());
    assertThrows(RuntimeException.class, () -> authService.getSupplierByEmail("missing@test.com"));
  }

  @Test
  void getConsumerByEmail_foundAndMissing() {
    ConsumerProfile consumer = new ConsumerProfile();
    consumer.setConsumerId(300L);

    when(consumerRepo.findByEmail("consumer@test.com")).thenReturn(Optional.of(consumer));
    assertEquals(300L, authService.getConsumerByEmail("consumer@test.com").getConsumerId());

    when(consumerRepo.findByEmail("missing@test.com")).thenReturn(Optional.empty());
    RuntimeException ex =
        assertThrows(
            RuntimeException.class, () -> authService.getConsumerByEmail("missing@test.com"));
    assertTrue(ex instanceof RuntimeException);
    verify(consumerRepo).findByEmail(eq("consumer@test.com"));
  }
}
