package com.frh.backend.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.ListingPhoto;
import com.frh.backend.dto.ListingDTO;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.service.ListingService;
import com.frh.backend.service.PhotoStorageService;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@WithMockUser(roles = {"CONSUMER", "SUPPLIER", "ADMIN"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ListingController.class)
class ListingControllerTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private ListingController listingController;

  @MockitoBean private ListingRepository listingRepository;

  @MockitoBean private StoreRepository storeRepository;

  @MockitoBean private ListingService listingService;

  @MockitoBean private PhotoStorageService photoStorageService;

  @Autowired private ObjectMapper objectMapper;

  /* -----------------------------
  HELPERS
  ----------------------------- */
  private Listing validListing() {
    Listing l = new Listing();
    l.setTitle("Bread");
    l.setDescription("Fresh bread");
    l.setOriginalPrice(BigDecimal.valueOf(10));
    l.setRescuePrice(BigDecimal.valueOf(5));
    l.setPickupStart(LocalDateTime.now().plusHours(1));
    l.setPickupEnd(LocalDateTime.now().plusHours(2));
    l.setExpiryAt(LocalDateTime.now().plusHours(3));
    return l;
  }

  private ListingDTO validListingDto() {
    ListingDTO l = new ListingDTO();
    l.setTitle("Bread");
    l.setDescription("Fresh bread");
    l.setOriginalPrice(BigDecimal.valueOf(10));
    l.setRescuePrice(BigDecimal.valueOf(5));
    l.setPickupStart(LocalDateTime.now().plusHours(1));
    l.setPickupEnd(LocalDateTime.now().plusHours(2));
    l.setExpiryAt(LocalDateTime.now().plusHours(3));
    return l;
  }

  private Listing listingWithPhotos(Long listingId) {
    Listing listing = validListing();
    listing.setListingId(listingId);

    ListingPhoto photo1 = new ListingPhoto();
    photo1.setPhotoUrl("/uploads/listings/old_1.jpg");
    photo1.setSortOrder(1);

    ListingPhoto photo2 = new ListingPhoto();
    photo2.setPhotoUrl("/uploads/listings/old_2.jpg");
    photo2.setSortOrder(2);

    listing.setPhotos(new ArrayList<>(List.of(photo1, photo2)));
    return listing;
  }

  /* -----------------------------
  CREATE – STORE MISSING
  ----------------------------- */
  @Test
  void createListing_storeMissing() throws Exception {

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validListingDto())))
        .andExpect(status().isBadRequest());
  }

  /* -----------------------------
  CREATE – STORE NOT FOUND
  ----------------------------- */
  @Test
  void createListing_storeNotFound() throws Exception {

    Mockito.when(storeRepository.existsById(1L)).thenReturn(false);

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validListingDto())))
        .andExpect(status().isBadRequest());
  }

  /* -----------------------------
  CREATE – VALIDATION ERROR
  ----------------------------- */
  @Test
  void createListing_validationError() throws Exception {

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    ListingDTO invalid = new ListingDTO(); // missing fields

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
  }

  /* -----------------------------
  CREATE – SUCCESS
  ----------------------------- */
  @Test
  void createListing_success() throws Exception {

    ListingDTO listing = validListingDto();
    listing.setListingId(1L);

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    Mockito.when(listingService.createListing(Mockito.any(), Mockito.eq(1L))).thenReturn(listing);

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validListingDto())))
        .andExpect(status().isOk());
  }

  /* -----------------------------
  CREATE – DB ERROR
  ----------------------------- */
  @Test
  void createListing_dbError() throws Exception {

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    Mockito.when(listingService.createListing(Mockito.any(), Mockito.eq(1L)))
        .thenThrow(new DataIntegrityViolationException("constraint"));

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validListingDto())))
        .andExpect(status().isBadRequest());
  }

  /* -----------------------------
  CREATE - UNEXPECTED ERROR
  ----------------------------- */
  @Test
  void createListing_unexpectedError() throws Exception {

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    Mockito.when(listingService.createListing(Mockito.any(), Mockito.eq(1L)))
        .thenThrow(new RuntimeException("Something went wrong"));

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validListingDto())))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(containsString("Unexpected server error")));
  }

  /* -----------------------------
  GET ALL
  ----------------------------- */
  @Test
  void getAllListings() throws Exception {

    Mockito.when(listingRepository.findAll()).thenReturn(List.of(new Listing()));

    mockMvc.perform(get("/api/supplier/listings")).andExpect(status().isOk());
  }

  /* -----------------------------
  GET BY SUPPLIER
  ----------------------------- */
  @Test
  void getListingsBySupplier() throws Exception {

    Mockito.when(listingService.getListingsBySupplier(1L)).thenReturn(List.of(new ListingDTO()));

    mockMvc.perform(get("/api/supplier/listings/supplier/{id}", 1L)).andExpect(status().isOk());
  }

  /* -----------------------------
  GET BY ID – NOT FOUND
  ----------------------------- */
  @Test
  void getListingById_notFound() throws Exception {

    Mockito.when(listingRepository.findById(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/supplier/listings/{id}", 99L)).andExpect(status().isNotFound());
  }

  /* -----------------------------
  GET BY ID - FOUND
  ----------------------------- */
  @Test
  void getListingById_found() throws Exception {

    Listing listing = validListing();
    listing.setListingId(1L);

    Mockito.when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

    mockMvc.perform(get("/api/supplier/listings/{id}", 1L)).andExpect(status().isOk());
  }

  /* -----------------------------
  UPDATE – NOT FOUND
  ----------------------------- */
  @Test
  void updateListing_notFound() throws Exception {

    Mockito.when(listingRepository.existsById(1L)).thenReturn(false);

    mockMvc
        .perform(
            put("/api/supplier/listings/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validListing())))
        .andExpect(status().isNotFound());
  }

  /* -----------------------------
  UPDATE – SUCCESS
  ----------------------------- */
  @Test
  void updateListing_success() throws Exception {
    Mockito.when(listingRepository.existsById(1L)).thenReturn(true);

    Mockito.when(listingService.updateListing(Mockito.eq(1L), Mockito.any(ListingDTO.class)))
        .thenReturn(validListingDto());

    mockMvc
        .perform(
            put("/api/supplier/listings/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validListing())))
        .andExpect(status().isOk());

    Mockito.verify(listingService).updateListing(Mockito.eq(1L), Mockito.any(ListingDTO.class));
  }

  /* -----------------------------
  UPDATE - VALIDATION ERROR
  ----------------------------- */
  @Test
  void updateListing_validationError() throws Exception {
    Listing invalid = validListing();
    invalid.setRescuePrice(BigDecimal.valueOf(20));

    Mockito.when(listingRepository.existsById(1L)).thenReturn(true);

    mockMvc
        .perform(
            put("/api/supplier/listings/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(
            content().string(containsString("Rescue price must be lower than original price")));

    Mockito.verify(listingService, Mockito.never())
        .updateListing(Mockito.anyLong(), Mockito.any(ListingDTO.class));
  }

  /* -----------------------------
  DELETE – NOT FOUND
  ----------------------------- */
  @Test
  void deleteListing_notFound() throws Exception {

    Mockito.when(listingRepository.existsById(1L)).thenReturn(false);

    mockMvc.perform(delete("/api/supplier/listings/{id}", 1L)).andExpect(status().isNotFound());
  }

  /* -----------------------------
  DELETE – SUCCESS
  ----------------------------- */
  @Test
  void deleteListing_success() throws Exception {

    Mockito.when(listingRepository.existsById(1L)).thenReturn(true);

    mockMvc.perform(delete("/api/supplier/listings/{id}", 1L)).andExpect(status().isOk());
  }

  /* -----------------------------
  CREATE - TITLE BLANK
  ----------------------------- */
  @Test
  void createListing_titleBlank() throws Exception {

    ListingDTO invalid = validListingDto();
    invalid.setTitle("   ");

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Title is required")));
  }

  /* -----------------------------
  CREATE - ORIGINAL PRICE MUST BE POSITIVE
  ----------------------------- */
  @Test
  void createListing_originalPriceMustBePositive() throws Exception {

    ListingDTO invalid = validListingDto();
    invalid.setOriginalPrice(BigDecimal.ZERO);

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Original price must be greater than 0")));
  }

  /* -----------------------------
  CREATE - RESCUE PRICE CANNOT BE NEGATIVE
  ----------------------------- */
  @Test
  void createListing_rescuePriceCannotBeNegative() throws Exception {

    ListingDTO invalid = validListingDto();
    invalid.setRescuePrice(BigDecimal.valueOf(-1));

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Rescue price cannot be negative")));
  }

  /* -----------------------------
  CREATE - RESCUE PRICE MUST BE LOWER
  ----------------------------- */
  @Test
  void createListing_rescuePriceMustBeLowerThanOriginalPrice() throws Exception {

    ListingDTO invalid = validListingDto();
    invalid.setRescuePrice(BigDecimal.valueOf(10));

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(
            content().string(containsString("Rescue price must be lower than original price")));
  }

  /* -----------------------------
  CREATE - PICKUP START MUST BE IN FUTURE
  ----------------------------- */
  @Test
  void createListing_pickupStartMustBeInFuture() throws Exception {

    ListingDTO invalid = validListingDto();
    invalid.setPickupStart(LocalDateTime.now().minusHours(1));
    invalid.setPickupEnd(LocalDateTime.now().plusHours(1));
    invalid.setExpiryAt(LocalDateTime.now().plusHours(2));

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Pickup start time must be in the future")));
  }

  /* -----------------------------
  CREATE - PICKUP END BEFORE START
  ----------------------------- */
  @Test
  void createListing_pickupEndCannotBeBeforeStart() throws Exception {

    ListingDTO invalid = validListingDto();
    invalid.setPickupStart(LocalDateTime.now().plusHours(3));
    invalid.setPickupEnd(LocalDateTime.now().plusHours(2));
    invalid.setExpiryAt(LocalDateTime.now().plusHours(4));

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Pickup end time cannot be before start time")));
  }

  /* -----------------------------
  CREATE - EXPIRY BEFORE PICKUP END
  ----------------------------- */
  @Test
  void createListing_expiryMustBeAfterPickupEnd() throws Exception {

    ListingDTO invalid = validListingDto();
    invalid.setPickupStart(LocalDateTime.now().plusHours(1));
    invalid.setPickupEnd(LocalDateTime.now().plusHours(3));
    invalid.setExpiryAt(LocalDateTime.now().plusHours(2));

    Mockito.when(storeRepository.existsById(1L)).thenReturn(true);

    mockMvc
        .perform(
            post("/api/supplier/listings")
                .param("storeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Expiry time must be after pickup end time")));
  }

  @Test
  void uploadListingPhoto_nullFile_returnsBadRequest() {
    ResponseEntity<?> response = listingController.uploadListingPhoto(10L, null);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Photo file is required", response.getBody());
  }

  @Test
  void uploadListingPhoto_emptyFile_returnsBadRequest() {
    MultipartFile emptyFile = Mockito.mock(MultipartFile.class);
    Mockito.when(emptyFile.isEmpty()).thenReturn(true);

    ResponseEntity<?> response = listingController.uploadListingPhoto(10L, emptyFile);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Photo file is required", response.getBody());
  }

  @Test
  void uploadListingPhoto_listingNotFound_returnsNotFound() {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    Mockito.when(listingRepository.findById(77L)).thenReturn(Optional.empty());

    ResponseEntity<?> response = listingController.uploadListingPhoto(77L, file);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Listing not found with id: 77", response.getBody());
  }

  @Test
  void uploadListingPhoto_successWithExtension_updatesSortOrderAndReturnsUrl() throws Exception {
    Listing listing = listingWithPhotos(11L);

    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    String uploadedUrl = "https://cdn.example.com/listing_11_abc.png";

    Mockito.when(listingRepository.findById(11L)).thenReturn(Optional.of(listing));
    Mockito.when(photoStorageService.store(11L, file)).thenReturn(uploadedUrl);
    Mockito.when(listingRepository.save(any(Listing.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<?> response = listingController.uploadListingPhoto(11L, file);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    String relativeUrl = (String) response.getBody();
    assertEquals(uploadedUrl, relativeUrl);

    ArgumentCaptor<Listing> captor = ArgumentCaptor.forClass(Listing.class);
    verify(listingRepository).save(captor.capture());
    verify(photoStorageService).store(11L, file);
    Listing saved = captor.getValue();

    assertEquals(3, saved.getPhotos().size());
    assertTrue(
        saved.getPhotos().stream()
            .anyMatch(p -> p.getSortOrder() == 1 && relativeUrl.equals(p.getPhotoUrl())));
    assertTrue(
        saved.getPhotos().stream()
            .anyMatch(
                p ->
                    p.getSortOrder() == 2
                        && "/uploads/listings/old_1.jpg".equals(p.getPhotoUrl())));
    assertTrue(
        saved.getPhotos().stream()
            .anyMatch(
                p ->
                    p.getSortOrder() == 3
                        && "/uploads/listings/old_2.jpg".equals(p.getPhotoUrl())));
  }

  @Test
  void uploadListingPhoto_successWithoutExtension_handlesFilename() throws Exception {
    Listing listing = validListing();
    listing.setListingId(12L);
    listing.setPhotos(new ArrayList<>());

    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    String uploadedUrl = "https://cdn.example.com/listing_12_noext";

    Mockito.when(listingRepository.findById(12L)).thenReturn(Optional.of(listing));
    Mockito.when(photoStorageService.store(12L, file)).thenReturn(uploadedUrl);
    Mockito.when(listingRepository.save(any(Listing.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<?> response = listingController.uploadListingPhoto(12L, file);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(uploadedUrl, response.getBody());
    verify(photoStorageService).store(12L, file);
  }

  @Test
  void uploadListingPhoto_nullOriginalFilename_handlesFilename() throws Exception {
    Listing listing = validListing();
    listing.setListingId(13L);
    listing.setPhotos(new ArrayList<>());

    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    String uploadedUrl = "https://cdn.example.com/listing_13";

    Mockito.when(listingRepository.findById(13L)).thenReturn(Optional.of(listing));
    Mockito.when(photoStorageService.store(13L, file)).thenReturn(uploadedUrl);
    Mockito.when(listingRepository.save(any(Listing.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<?> response = listingController.uploadListingPhoto(13L, file);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(uploadedUrl, response.getBody());
    verify(photoStorageService).store(13L, file);
  }

  @Test
  void uploadListingPhoto_ioException_returnsInternalServerError() throws Exception {
    Listing listing = validListing();
    listing.setListingId(14L);
    listing.setPhotos(new ArrayList<>());

    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(false);
    Mockito.when(photoStorageService.store(14L, file)).thenThrow(new IOException("storage down"));

    Mockito.when(listingRepository.findById(14L)).thenReturn(Optional.of(listing));

    ResponseEntity<?> response = listingController.uploadListingPhoto(14L, file);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Failed to store photo", response.getBody());
  }

  @SuppressWarnings("unchecked")
  private List<String> invokeValidateListing(Listing listing) throws Exception {
    Method validateListingMethod =
        ListingController.class.getDeclaredMethod("validateListing", Listing.class);
    validateListingMethod.setAccessible(true);
    return (List<String>) validateListingMethod.invoke(listingController, listing);
  }

  @Test
  void validateListing_validInput_returnsNoErrors() throws Exception {
    Listing listing = validListing();

    List<String> errors = invokeValidateListing(listing);

    assertTrue(errors.isEmpty());
  }

  @Test
  void validateListing_missingRequiredFields_returnsRequiredFieldErrors() throws Exception {
    Listing listing = validListing();
    listing.setTitle(null);
    listing.setOriginalPrice(null);
    listing.setRescuePrice(null);
    listing.setPickupStart(null);
    listing.setPickupEnd(null);
    listing.setExpiryAt(null);

    List<String> errors = invokeValidateListing(listing);

    assertTrue(errors.contains("Title is required"));
    assertTrue(errors.contains("Original price is required"));
    assertTrue(errors.contains("Rescue price is required"));
    assertTrue(errors.contains("Pickup start time is required"));
    assertTrue(errors.contains("Pickup end time is required"));
    assertTrue(errors.contains("Expiry time is required"));
  }

  @Test
  void validateListing_blankTitle_hitsTrimBranch() throws Exception {
    Listing listing = validListing();
    listing.setTitle("   ");

    List<String> errors = invokeValidateListing(listing);

    assertTrue(errors.contains("Title is required"));
  }

  @Test
  void validateListing_invalidTimingAndPricing_returnsRelationshipErrors() throws Exception {
    Listing listing = validListing();
    listing.setRescuePrice(BigDecimal.valueOf(10));
    listing.setOriginalPrice(BigDecimal.valueOf(10));
    listing.setPickupStart(LocalDateTime.now().minusMinutes(5));
    listing.setPickupEnd(LocalDateTime.now().minusMinutes(10));
    listing.setExpiryAt(LocalDateTime.now().minusMinutes(15));

    List<String> errors = invokeValidateListing(listing);

    assertTrue(errors.contains("Rescue price must be lower than original price"));
    assertTrue(errors.contains("Pickup start time must be in the future"));
    assertTrue(errors.contains("Pickup end time cannot be before start time"));
    assertTrue(errors.contains("Expiry time must be after pickup end time"));
  }
}
