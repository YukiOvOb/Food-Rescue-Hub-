package com.frh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.frh.backend.Model.ConsumerStats;
import com.frh.backend.Model.Inventory;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.ListingPhoto;
import com.frh.backend.Model.ListingStats;
import com.frh.backend.Model.Store;
import com.frh.backend.Model.StoreStats;
import com.frh.backend.Model.StoreType;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.Model.UserStoreInteraction;
import com.frh.backend.dto.StoreRecommendationDTO;
import com.frh.backend.repository.ConsumerStatsRepository;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.ListingStatsRepository;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.repository.StoreStatsRepository;
import com.frh.backend.repository.UserStoreInteractionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

  @Mock private ListingRepository listingRepository;

  @Mock private StoreRepository storeRepository;

  @Mock private ConsumerStatsRepository consumerStatsRepository;

  @Mock private ListingStatsRepository listingStatsRepository;

  @Mock private StoreStatsRepository storeStatsRepository;

  @Mock private UserStoreInteractionRepository userStoreInteractionRepository;

  @InjectMocks private RecommendationService recommendationService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(
        recommendationService, "recommendationServiceUrl", "http://rec-service");

    lenient()
        .when(consumerStatsRepository.findByConsumerId(anyLong()))
        .thenReturn(Optional.empty());
    lenient().when(listingStatsRepository.findByListingId(anyLong())).thenReturn(Optional.empty());
    lenient().when(storeStatsRepository.findByStoreId(anyLong())).thenReturn(Optional.empty());
    lenient()
        .when(userStoreInteractionRepository.findByConsumerIdAndStoreId(anyLong(), anyLong()))
        .thenReturn(Optional.empty());
    lenient().when(storeRepository.findById(anyLong())).thenReturn(Optional.empty());
    lenient().when(listingRepository.findById(anyLong())).thenReturn(Optional.empty());
  }

  @Test
  void recommendStoresForHomepage_noActiveListings_returnsEmpty() {
    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of());

    List<StoreRecommendationDTO> result =
        recommendationService.recommendStoresForHomepage(1L, null, 1.30, 103.80);

    assertTrue(result.isEmpty());
  }

  @Test
  void recommendStoresForHomepage_success_mapsDtoAndEnrichment() {
    Listing listing =
        buildListing(
            100L, 50L, "Bakery Box", "Bakery", new BigDecimal("10.00"), new BigDecimal("4.00"));
    ConsumerStats consumerStats = new ConsumerStats();
    consumerStats.setTotalOrders(6);
    consumerStats.setCompletedOrders(4);
    consumerStats.setTotalSpend(new BigDecimal("88.00"));
    consumerStats.setAvgOrderValue(new BigDecimal("22.00"));
    consumerStats.setFavoriteStoreType("Bakery");

    ListingStats listingStats = new ListingStats();
    listingStats.setViewCount(10);
    listingStats.setClickCount(5);
    listingStats.setOrderCount(2);
    listingStats.setCtr(new BigDecimal("0.5000"));

    StoreStats storeStats = new StoreStats();
    storeStats.setAvgRating(new BigDecimal("4.70"));
    storeStats.setTotalOrders(100);
    storeStats.setCompletionRate(new BigDecimal("0.9000"));
    storeStats.setOnTimeRate(new BigDecimal("0.9500"));

    UserStoreInteraction userStoreInteraction = new UserStoreInteraction();
    userStoreInteraction.setOrderCount(3);

    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(listing));
    when(consumerStatsRepository.findByConsumerId(7L)).thenReturn(Optional.of(consumerStats));
    when(listingStatsRepository.findByListingId(100L)).thenReturn(Optional.of(listingStats));
    when(storeStatsRepository.findByStoreId(50L)).thenReturn(Optional.of(storeStats));
    when(userStoreInteractionRepository.findByConsumerIdAndStoreId(7L, 50L))
        .thenReturn(Optional.of(userStoreInteraction));
    when(storeRepository.findById(50L)).thenReturn(Optional.of(listing.getStore()));
    when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"recommendations\":[{\"store_id\":50,\"listing_id\":100,\"store_name\":\"Store"
                    + " 50\",\"listing_title\":\"Bakery"
                    + " Box\",\"rescue_price\":4.0,\"original_price\":10.0,"
                    + "\"distance\":1.2,\"store_avg_rating\":4.7,\"predicted_score\":0.93,"
                    + "\"photo_url\":\"https://img/100.png\",\"store_type\":\"Bakery\",\"qty_available\":5}]}",
                MediaType.APPLICATION_JSON));

    List<StoreRecommendationDTO> result =
        recommendationService.recommendStoresForHomepage(7L, null, 1.30, 103.80);
    server.verify();

    assertEquals(1, result.size());
    StoreRecommendationDTO dto = result.get(0);
    assertEquals(50L, dto.getStoreId());
    assertEquals(100L, dto.getListingId());
    assertEquals("Bakery Box", dto.getTitle());
    assertEquals(60, dto.getSavingsPercentage());
    assertEquals("High rating store", dto.getRecommendationReason());
    assertEquals("Store 50", dto.getStoreName());
    assertEquals("Address 50", dto.getAddressLine());
    assertNotNull(dto.getPickupStart());
    assertNotNull(dto.getPickupEnd());
  }

  @Test
  void recommendStoresForHomepage_recommendationServiceError_returnsEmpty() {
    Listing listing =
        buildListing(101L, 51L, "Pasta", "Italian", new BigDecimal("9.00"), new BigDecimal("6.00"));
    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(listing));

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withServerError());

    List<StoreRecommendationDTO> result =
        recommendationService.recommendStoresForHomepage(1L, 5, 1.30, 103.80);
    server.verify();

    assertTrue(result.isEmpty());
  }

  @Test
  void searchWithRecommendations_blankQuery_returnsEmpty() {
    List<StoreRecommendationDTO> result =
        recommendationService.searchWithRecommendations(1L, "   ", 5, 1.30, 103.80);
    assertTrue(result.isEmpty());
  }

  @Test
  void searchWithRecommendations_filtersAndRespectsTopK() {
    Listing matchByTitle =
        buildListing(
            201L, 61L, "Chicken Rice", "Hawker", new BigDecimal("8.00"), new BigDecimal("5.00"));
    Listing matchByDescription =
        buildListing(
            202L, 62L, "Meal Set", "Hawker", new BigDecimal("10.00"), new BigDecimal("7.00"));
    matchByDescription.setDescription("Great rice bowl");
    Listing notMatched =
        buildListing(
            203L, 63L, "Pasta", "Italian", new BigDecimal("12.00"), new BigDecimal("8.00"));
    notMatched.setDescription("Creamy sauce");

    when(listingRepository.findByStatus("ACTIVE"))
        .thenReturn(List.of(matchByTitle, matchByDescription, notMatched));

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"recommendations\":[{\"store_id\":62,\"listing_id\":202,\"store_name\":\"Store"
                    + " 62\",\"listing_title\":\"Meal"
                    + " Set\",\"rescue_price\":7.0,\"original_price\":10.0,"
                    + "\"distance\":1.9,\"store_avg_rating\":4.1,\"predicted_score\":0.91,"
                    + "\"photo_url\":\"https://img/202.png\",\"store_type\":\"Hawker\"},{\"store_id\":61,\"listing_id\":201,\"store_name\":\"Store"
                    + " 61\",\"listing_title\":\"Chicken"
                    + " Rice\",\"rescue_price\":5.0,\"original_price\":8.0,"
                    + "\"distance\":3.2,\"store_avg_rating\":4.0,\"predicted_score\":0.77,"
                    + "\"photo_url\":\"https://img/201.png\",\"store_type\":\"Hawker\"}]}",
                MediaType.APPLICATION_JSON));

    List<StoreRecommendationDTO> result =
        recommendationService.searchWithRecommendations(5L, "rice", 1, 1.30, 103.80);
    server.verify();

    assertEquals(1, result.size());
    assertEquals(202L, result.get(0).getListingId());
  }

  @Test
  void searchWithRecommendations_noKeywordMatches_returnsEmpty() {
    Listing listing =
        buildListing(
            300L, 70L, "Burger", "FastFood", new BigDecimal("11.00"), new BigDecimal("7.00"));
    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(listing));

    List<StoreRecommendationDTO> result =
        recommendationService.searchWithRecommendations(2L, "sushi", 5, 1.30, 103.80);

    assertTrue(result.isEmpty());
  }

  @Test
  void recommendStoresForHomepage_generatesReasonBranches() {
    Listing listing =
        buildListing(
            400L, 80L, "Daily Box", "Mixed", new BigDecimal("20.00"), new BigDecimal("10.00"));
    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(listing));

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"recommendations\":[{\"store_id\":80,\"listing_id\":400,\"store_name\":\"Store"
                    + " 80\",\"listing_title\":\"A\","
                    + "\"rescue_price\":10.0,\"original_price\":20.0,\"distance\":3.0,\"store_avg_rating\":4.8,"
                    + "\"predicted_score\":0.9,\"photo_url\":\"x\",\"store_type\":\"Mixed\"},{\"store_id\":80,\"listing_id\":400,\"store_name\":\"Store"
                    + " 80\",\"listing_title\":\"B\","
                    + "\"rescue_price\":16.0,\"original_price\":20.0,\"distance\":1.5,\"store_avg_rating\":4.0,"
                    + "\"predicted_score\":0.8,\"photo_url\":\"x\",\"store_type\":\"Mixed\"},{\"store_id\":80,\"listing_id\":400,\"store_name\":\"Store"
                    + " 80\",\"listing_title\":\"C\","
                    + "\"rescue_price\":5.0,\"original_price\":20.0,\"distance\":4.0,\"store_avg_rating\":3.8,"
                    + "\"predicted_score\":0.7,\"photo_url\":\"x\",\"store_type\":\"Mixed\"},{\"store_id\":80,\"listing_id\":400,\"store_name\":\"Store"
                    + " 80\",\"listing_title\":\"D\","
                    + "\"rescue_price\":15.0,\"original_price\":20.0,\"distance\":4.0,\"store_avg_rating\":3.8,"
                    + "\"predicted_score\":0.6,\"photo_url\":\"x\",\"store_type\":\"Mixed\"}]}",
                MediaType.APPLICATION_JSON));

    List<StoreRecommendationDTO> result =
        recommendationService.recommendStoresForHomepage(8L, 10, 1.30, 103.80);
    server.verify();

    assertEquals(4, result.size());
    assertEquals("High rating store", result.get(0).getRecommendationReason());
    assertEquals("Nearby location", result.get(1).getRecommendationReason());
    assertEquals("Great discount available", result.get(2).getRecommendationReason());
    assertEquals("Recommended for you", result.get(3).getRecommendationReason());
  }

  @Test
  void recommendStoresForHomepage_handlesFallbackFeatureBranches() {
    Listing listing =
        buildListing(
            500L, 90L, "Fallback Box", "Bakery", new BigDecimal("12.00"), new BigDecimal("9.00"));
    listing.getStore().setSupplierProfile(null);
    listing.getStore().setLat(null);
    listing.getStore().setLng(null);
    listing.setInventory(null);
    listing.setPhotos(null);

    ConsumerStats consumerStats = new ConsumerStats();
    consumerStats.setTotalOrders(2);
    consumerStats.setCompletedOrders(0);
    consumerStats.setTotalSpend(new BigDecimal("12.00"));
    consumerStats.setAvgOrderValue(null);
    consumerStats.setFavoriteStoreType("Bakery");

    ListingStats listingStats = new ListingStats();
    listingStats.setViewCount(5);
    listingStats.setClickCount(1);
    listingStats.setOrderCount(1);
    listingStats.setCtr(null);

    StoreStats storeStats = new StoreStats();
    storeStats.setAvgRating(null);
    storeStats.setTotalOrders(7);
    storeStats.setCompletionRate(null);
    storeStats.setOnTimeRate(null);

    UserStoreInteraction userStoreInteraction = new UserStoreInteraction();
    userStoreInteraction.setOrderCount(0);

    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(listing));
    when(consumerStatsRepository.findByConsumerId(10L)).thenReturn(Optional.of(consumerStats));
    when(listingStatsRepository.findByListingId(500L)).thenReturn(Optional.of(listingStats));
    when(storeStatsRepository.findByStoreId(90L)).thenReturn(Optional.of(storeStats));
    when(userStoreInteractionRepository.findByConsumerIdAndStoreId(10L, 90L))
        .thenReturn(Optional.of(userStoreInteraction));
    when(storeRepository.findById(90L)).thenReturn(Optional.of(listing.getStore()));

    Listing listingWithNullPickup =
        buildListing(
            500L, 90L, "Fallback Box", "Bakery", new BigDecimal("12.00"), new BigDecimal("9.00"));
    listingWithNullPickup.setPickupStart(null);
    listingWithNullPickup.setPickupEnd(null);
    when(listingRepository.findById(500L)).thenReturn(Optional.of(listingWithNullPickup));

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"recommendations\":[{\"store_id\":90,\"listing_id\":500,\"store_name\":\"Store"
                    + " 90\",\"listing_title\":\"Fallback"
                    + " Box\",\"rescue_price\":9.0,\"original_price\":12.0,"
                    + "\"distance\":999.0,\"store_avg_rating\":3.6,\"predicted_score\":0.33,"
                    + "\"photo_url\":\"\",\"store_type\":\"Unknown\"}]}",
                MediaType.APPLICATION_JSON));

    List<StoreRecommendationDTO> result =
        recommendationService.recommendStoresForHomepage(10L, 5, null, null);
    server.verify();

    assertEquals(1, result.size());
    StoreRecommendationDTO dto = result.get(0);
    assertEquals("Unknown", dto.getCategory());
    assertEquals(0, dto.getQtyAvailable());
    assertNull(dto.getLat());
    assertNull(dto.getLng());
    assertNull(dto.getPickupStart());
    assertNull(dto.getPickupEnd());
    assertEquals("Recommended for you", dto.getRecommendationReason());
  }

  @Test
  void recommendStoresForHomepage_nonOkResponse_returnsEmpty() {
    Listing listing =
        buildListing(510L, 91L, "Soup", "Mixed", new BigDecimal("8.00"), new BigDecimal("6.00"));
    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(listing));

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON).body("{}"));

    List<StoreRecommendationDTO> result =
        recommendationService.recommendStoresForHomepage(10L, 5, 1.30, 103.80);
    server.verify();

    assertTrue(result.isEmpty());
  }

  @Test
  void recommendStoresForHomepage_okNullBody_returnsEmpty() {
    Listing listing =
        buildListing(511L, 92L, "Rice", "Mixed", new BigDecimal("9.00"), new BigDecimal("7.00"));
    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(listing));

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

    List<StoreRecommendationDTO> result =
        recommendationService.recommendStoresForHomepage(10L, 5, 1.30, 103.80);
    server.verify();

    assertTrue(result.isEmpty());
  }

  @Test
  void recommendStoresForHomepage_enrichmentException_isHandled() {
    Listing listing =
        buildListing(
            520L, 93L, "Noodles", "Asian", new BigDecimal("10.00"), new BigDecimal("8.00"));
    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(listing));
    when(storeRepository.findById(93L)).thenThrow(new RuntimeException("db failure"));

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"recommendations\":[{\"store_id\":93,\"listing_id\":520,\"store_name\":\"Store"
                    + " 93\","
                    + "\"listing_title\":\"Noodles\",\"rescue_price\":8.0,\"original_price\":10.0,"
                    + "\"distance\":2.5,\"store_avg_rating\":4.0,\"predicted_score\":0.5,"
                    + "\"photo_url\":\"x\",\"store_type\":\"Asian\",\"qty_available\":2}]}",
                MediaType.APPLICATION_JSON));

    List<StoreRecommendationDTO> result =
        recommendationService.recommendStoresForHomepage(4L, 5, 1.30, 103.80);
    server.verify();

    assertEquals(1, result.size());
    assertEquals(93L, result.get(0).getStoreId());
  }

  @Test
  void searchWithRecommendations_nullQuery_returnsEmpty() {
    List<StoreRecommendationDTO> result =
        recommendationService.searchWithRecommendations(1L, null, 5, 1.30, 103.80);
    assertTrue(result.isEmpty());
  }

  @Test
  void searchWithRecommendations_handlesNullTitleAndDescription_andReturnsAllWhenTopKNull() {
    Listing titleNull =
        buildListing(600L, 100L, "A", "Mixed", new BigDecimal("10.00"), new BigDecimal("8.00"));
    titleNull.setTitle(null);
    titleNull.setDescription("Rice bowl");

    Listing descNull =
        buildListing(
            601L, 101L, "Rice Deal", "Mixed", new BigDecimal("9.00"), new BigDecimal("6.00"));
    descNull.setDescription(null);

    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(titleNull, descNull));

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"recommendations\":[{\"store_id\":100,\"listing_id\":600,\"store_name\":\"Store"
                    + " 100\",\"listing_title\":\"Rice bowl\","
                    + "\"rescue_price\":8.0,\"original_price\":10.0,\"distance\":2.1,\"store_avg_rating\":4.0,"
                    + "\"predicted_score\":0.8,\"photo_url\":\"x\",\"store_type\":\"Mixed\"},{\"store_id\":101,\"listing_id\":601,\"store_name\":\"Store"
                    + " 101\",\"listing_title\":\"Rice Deal\","
                    + "\"rescue_price\":6.0,\"original_price\":9.0,\"distance\":2.4,\"store_avg_rating\":3.9,"
                    + "\"predicted_score\":0.7,\"photo_url\":\"x\",\"store_type\":\"Mixed\"}]}",
                MediaType.APPLICATION_JSON));

    List<StoreRecommendationDTO> result =
        recommendationService.searchWithRecommendations(2L, "rice", null, 1.30, 103.80);
    server.verify();

    assertEquals(2, result.size());
  }

  @Test
  void searchWithRecommendations_topKEdgeCases_returnAll() {
    Listing listing =
        buildListing(
            610L, 110L, "Rice Pack", "Mixed", new BigDecimal("10.00"), new BigDecimal("7.00"));
    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(listing));

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"recommendations\":[{\"store_id\":110,\"listing_id\":610,\"store_name\":\"Store"
                    + " 110\",\"listing_title\":\"Rice"
                    + " Pack\",\"rescue_price\":7.0,\"original_price\":10.0,"
                    + "\"distance\":3.0,\"store_avg_rating\":4.0,\"predicted_score\":0.6,\"photo_url\":\"x\",\"store_type\":\"Mixed\"}]}",
                MediaType.APPLICATION_JSON));

    List<StoreRecommendationDTO> topKZero =
        recommendationService.searchWithRecommendations(2L, "rice", 0, 1.30, 103.80);
    assertEquals(1, topKZero.size());

    // second request for same URL
    server.reset();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"recommendations\":[{\"store_id\":110,\"listing_id\":610,\"store_name\":\"Store"
                    + " 110\",\"listing_title\":\"Rice"
                    + " Pack\",\"rescue_price\":7.0,\"original_price\":10.0,"
                    + "\"distance\":3.0,\"store_avg_rating\":4.0,\"predicted_score\":0.6,\"photo_url\":\"x\",\"store_type\":\"Mixed\"}]}",
                MediaType.APPLICATION_JSON));

    List<StoreRecommendationDTO> topKTooLarge =
        recommendationService.searchWithRecommendations(2L, "rice", 5, 1.30, 103.80);
    server.verify();
    assertEquals(1, topKTooLarge.size());
  }

  @Test
  void privateHelpers_coverNullBranches() {
    StoreRecommendationDTO dto = new StoreRecommendationDTO();
    dto.setDistance(null);

    String reason =
        ReflectionTestUtils.invokeMethod(
            recommendationService, "generateRecommendationReason", dto, null, 10);

    assertEquals("Recommended for you", reason);

    Double d1 =
        ReflectionTestUtils.invokeMethod(
            recommendationService, "calculateDistance", null, 103.8, 1.3, 103.9);
    Double d2 =
        ReflectionTestUtils.invokeMethod(
            recommendationService, "calculateDistance", 1.3, null, 1.3, 103.9);
    Double d3 =
        ReflectionTestUtils.invokeMethod(
            recommendationService, "calculateDistance", 1.3, 103.8, null, 103.9);
    Double d4 =
        ReflectionTestUtils.invokeMethod(
            recommendationService, "calculateDistance", 1.3, 103.8, 1.3, null);

    assertEquals(999.0, d1);
    assertEquals(999.0, d2);
    assertEquals(999.0, d3);
    assertEquals(999.0, d4);
  }

  @Test
  void recommendStoresForHomepage_responseWithZeroOriginalPrice_setsSavingsToZero() {
    Listing listing =
        buildListing(
            700L, 120L, "Zero Original", "Mixed", new BigDecimal("10.00"), new BigDecimal("5.00"));
    when(listingRepository.findByStatus("ACTIVE")).thenReturn(List.of(listing));

    Map<String, Object> rec = new HashMap<>();
    rec.put("store_id", 120);
    rec.put("listing_id", 700);
    rec.put("store_name", "Store 120");
    rec.put("listing_title", "Zero Original");
    rec.put("rescue_price", 5.0);
    rec.put("original_price", 0.0);
    rec.put("distance", 3.0);
    rec.put("store_avg_rating", 4.0);
    rec.put("predicted_score", 0.8);
    rec.put("photo_url", "x");
    rec.put("store_type", "Mixed");
    rec.put("qty_available", 1);

    MockRestServiceServer server = server();
    server
        .expect(requestTo("http://rec-service/api/recommend/stores"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"recommendations\":[" + toJsonObject(rec) + "]}", MediaType.APPLICATION_JSON));

    List<StoreRecommendationDTO> result =
        recommendationService.recommendStoresForHomepage(3L, 5, 1.30, 103.80);
    server.verify();

    assertEquals(1, result.size());
    assertEquals(0, result.get(0).getSavingsPercentage());
  }

  private MockRestServiceServer server() {
    RestTemplate restTemplate =
        (RestTemplate) ReflectionTestUtils.getField(recommendationService, "restTemplate");
    return MockRestServiceServer.bindTo(restTemplate).build();
  }

  private static String toJsonObject(Map<String, Object> map) {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String, Object> e : map.entrySet()) {
      if (!first) {
        sb.append(",");
      }
      first = false;
      sb.append("\"").append(e.getKey()).append("\":");
      Object value = e.getValue();
      if (value instanceof Number) {
        sb.append(value);
      } else {
        sb.append("\"").append(value).append("\"");
      }
    }
    sb.append("}");
    return sb.toString();
  }

  private static Listing buildListing(
      Long listingId,
      Long storeId,
      String title,
      String storeTypeName,
      BigDecimal originalPrice,
      BigDecimal rescuePrice) {
    StoreType storeType = new StoreType();
    storeType.setTypeName(storeTypeName);

    SupplierProfile supplier = new SupplierProfile();
    supplier.setSupplierId(storeId + 1000);
    supplier.setStoreType(storeType);

    Store store = new Store();
    store.setStoreId(storeId);
    store.setStoreName("Store " + storeId);
    store.setAddressLine("Address " + storeId);
    store.setLat(new BigDecimal("1.3000000"));
    store.setLng(new BigDecimal("103.8000000"));
    store.setSupplierProfile(supplier);

    Listing listing = new Listing();
    listing.setListingId(listingId);
    listing.setStore(store);
    listing.setTitle(title);
    listing.setDescription("Fresh " + title);
    listing.setOriginalPrice(originalPrice);
    listing.setRescuePrice(rescuePrice);
    listing.setPickupStart(LocalDateTime.now().plusHours(1));
    listing.setPickupEnd(LocalDateTime.now().plusHours(2));
    listing.setExpiryAt(LocalDateTime.now().plusHours(6));
    listing.setStatus("ACTIVE");

    Inventory inventory = new Inventory();
    inventory.setListing(listing);
    inventory.setQtyAvailable(10);
    inventory.setQtyReserved(0);
    listing.setInventory(inventory);

    ListingPhoto photo = new ListingPhoto();
    photo.setPhotoUrl("https://img/" + listingId + ".png");
    photo.setSortOrder(1);
    listing.setPhotos(List.of(photo));

    return listing;
  }
}
