package com.frh.backend.service;

import com.frh.backend.dto.StoreRecommendationDTO;
import com.frh.backend.Model.*;
import com.frh.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐系统服务
 * 为Android首页提供个性化商家推荐
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    // 推荐服务URL(Python Flask服务)
    @Value("${recommendation.service.url:http://localhost:5000}")
    private String recommendationServiceUrl;

    // Repositories
    private final ListingRepository listingRepository;
    private final StoreRepository storeRepository;
    private final ConsumerStatsRepository consumerStatsRepository;
    private final ListingStatsRepository listingStatsRepository;
    private final StoreStatsRepository storeStatsRepository;
    private final UserStoreInteractionRepository userStoreInteractionRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 为用户推荐商家(用于首页显示)
     *
     * @param consumerId 用户ID
     * @param topK       返回前K个商家(默认5个)
     * @param userLat    用户纬度(可选,用于计算距离)
     * @param userLng    用户经度(可选,用于计算距离)
     * @return 推荐的商家列表
     */
    public List<StoreRecommendationDTO> recommendStoresForHomepage(
            Long consumerId,
            Integer topK,
            Double userLat,
            Double userLng
    ) {
        if (topK == null) {
            topK = 5;
        }

        // 1. 获取所有活跃的listings作为候选
        List<Listing> activeListings = listingRepository.findByStatus("ACTIVE");

        if (activeListings.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 获取用户统计信息
        ConsumerStats consumerStats = consumerStatsRepository
                .findByConsumerId(consumerId)
                .orElse(null);

        // 3. 构造候选特征并调用推荐服务
        Map<String, Object> requestBody = buildRecommendationRequest(
                consumerId,
                activeListings,
                consumerStats,
                userLat,
                userLng,
                topK
        );

        // 4. 调用Python推荐服务
        List<Map<String, Object>> recommendations = callRecommendationService(requestBody);

        // 5. 转换为DTO
        List<StoreRecommendationDTO> result = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> rec : recommendations) {
            StoreRecommendationDTO dto = convertToDTO(rec, rank++, userLat, userLng);
            if (dto != null) {
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 构造推荐请求
     */
    private Map<String, Object> buildRecommendationRequest(
            Long consumerId,
            List<Listing> listings,
            ConsumerStats consumerStats,
            Double userLat,
            Double userLng,
            Integer topK
    ) {
        // 获取用户偏好店铺类型
        String favoriteStoreType = (consumerStats != null)
                ? consumerStats.getFavoriteStoreType()
                : "Unknown";

        // 构造候选列表
        List<Map<String, Object>> candidates = new ArrayList<>();

        for (Listing listing : listings) {
            Map<String, Object> candidate = new HashMap<>();

            // 基本信息
            candidate.put("listing_id", listing.getListingId());
            candidate.put("store_id", listing.getStore().getStoreId());
            candidate.put("store_name", listing.getStore().getStoreName());
            candidate.put("listing_title", listing.getTitle());

            // 店铺类型
            String storeType = listing.getStore().getSupplierProfile() != null
                    && listing.getStore().getSupplierProfile().getStoreType() != null
                    ? listing.getStore().getSupplierProfile().getStoreType().getTypeName()
                    : "Unknown";
            candidate.put("store_type", storeType);

            // 价格特征
            candidate.put("original_price", listing.getOriginalPrice().doubleValue());
            candidate.put("rescue_price", listing.getRescuePrice().doubleValue());
            double discountRate = 1.0 - listing.getRescuePrice()
                    .divide(listing.getOriginalPrice(), 4, RoundingMode.HALF_UP)
                    .doubleValue();
            candidate.put("discount_rate", discountRate);

            // 距离特征
            Double storeLat = listing.getStore().getLat() != null
                ? listing.getStore().getLat().doubleValue()
                : null;
            Double storeLng = listing.getStore().getLng() != null
                ? listing.getStore().getLng().doubleValue()
                : null;
            Double distance = calculateDistance(userLat, userLng, storeLat, storeLng);
            candidate.put("distance", distance);
            candidate.put("within_radius", distance != null && distance <= 5.0 ? 1 : 0);

            // 库存特征
            Integer qtyAvailable = (listing.getInventory() != null)
                    ? listing.getInventory().getQtyAvailable()
                    : 0;
            candidate.put("qty_available", qtyAvailable);

            // Listing统计特征
            ListingStats listingStats = listingStatsRepository
                    .findByListingId(listing.getListingId())
                    .orElse(null);

            candidate.put("listing_view_count", listingStats != null ? listingStats.getViewCount() : 0);
            candidate.put("listing_click_count", listingStats != null ? listingStats.getClickCount() : 0);
            candidate.put("listing_order_count", listingStats != null ? listingStats.getOrderCount() : 0);
            candidate.put("listing_ctr", listingStats != null && listingStats.getCtr() != null
                    ? listingStats.getCtr().doubleValue()
                    : 0.0);

            // 店铺统计特征
            StoreStats storeStats = storeStatsRepository
                    .findByStoreId(listing.getStore().getStoreId())
                    .orElse(null);

            candidate.put("store_avg_rating", storeStats != null && storeStats.getAvgRating() != null
                    ? storeStats.getAvgRating().doubleValue()
                    : 0.0);
            candidate.put("store_total_orders", storeStats != null ? storeStats.getTotalOrders() : 0);
            candidate.put("store_completion_rate", storeStats != null && storeStats.getCompletionRate() != null
                    ? storeStats.getCompletionRate().doubleValue()
                    : 0.0);
            candidate.put("store_on_time_rate", storeStats != null && storeStats.getOnTimeRate() != null
                    ? storeStats.getOnTimeRate().doubleValue()
                    : 0.0);

            // 用户特征
            candidate.put("user_total_orders", consumerStats != null ? consumerStats.getTotalOrders() : 0);
            candidate.put("user_total_spend", consumerStats != null
                    ? consumerStats.getTotalSpend().doubleValue()
                    : 0.0);
            candidate.put("user_avg_order_value", consumerStats != null && consumerStats.getAvgOrderValue() != null
                    ? consumerStats.getAvgOrderValue().doubleValue()
                    : 0.0);

            // 用户-店铺交互特征
            UserStoreInteraction userStoreInteraction = userStoreInteractionRepository
                    .findByConsumerIdAndStoreId(consumerId, listing.getStore().getStoreId())
                    .orElse(null);

            candidate.put("user_has_ordered_from_store", userStoreInteraction != null
                    && userStoreInteraction.getOrderCount() > 0 ? 1 : 0);
            candidate.put("user_store_order_count", userStoreInteraction != null
                    ? userStoreInteraction.getOrderCount()
                    : 0);

            // 上下文特征
            LocalDateTime now = LocalDateTime.now();
            candidate.put("day_of_week", now.getDayOfWeek().getValue());
            candidate.put("hour_of_day", now.getHour());
            candidate.put("is_weekend", (now.getDayOfWeek().getValue() >= 6) ? 1 : 0);

            // 时间特征
            long timeUntilPickupStart = ChronoUnit.HOURS.between(now, listing.getPickupStart());
            long timeUntilExpiry = ChronoUnit.HOURS.between(now, listing.getExpiryAt());
            long pickupWindowDuration = ChronoUnit.HOURS.between(listing.getPickupStart(), listing.getPickupEnd());

            candidate.put("time_until_pickup_start", timeUntilPickupStart);
            candidate.put("time_until_expiry", timeUntilExpiry);
            candidate.put("pickup_window_duration", pickupWindowDuration);

            // 用户偏好匹配
            candidate.put("user_favorite_store_type", favoriteStoreType);
            candidate.put("user_store_type_order_count", storeType.equals(favoriteStoreType) ? 1 : 0);

            // 页面上下文(首页)
            candidate.put("page_context", "homepage");

            // 时间段
            int hour = now.getHour();
            String timePeriod;
            if (hour >= 6 && hour < 12) timePeriod = "morning";
            else if (hour >= 12 && hour < 18) timePeriod = "afternoon";
            else if (hour >= 18 && hour < 22) timePeriod = "evening";
            else timePeriod = "night";
            candidate.put("time_period", timePeriod);

            // 搜索特征(首页场景下为0)
            candidate.put("query_title_similarity", 0.0);
            candidate.put("query_store_type_match", 0);
            candidate.put("query_dietary_match", 0);

            // 膳食匹配(简化版,默认0)
            candidate.put("dietary_match_count", 0);

            // 用户注册天数(简化版,假设都是老用户)
            candidate.put("user_days_since_registration", 30);
            candidate.put("user_days_since_last_order", consumerStats != null && consumerStats.getCompletedOrders() > 0 ? 7 : 999);

            // 图片URL(取第一张)
            String photoUrl = "";
            if (listing.getPhotos() != null && !listing.getPhotos().isEmpty()) {
                photoUrl = listing.getPhotos().get(0).getPhotoUrl();
            }
            candidate.put("photo_url", photoUrl);

            candidates.add(candidate);
        }

        // 构造请求body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", consumerId);
        requestBody.put("candidates", candidates);
        requestBody.put("top_k", topK);

        return requestBody;
    }

    /**
     * 调用Python推荐服务
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> callRecommendationService(Map<String, Object> requestBody) {
        try {
            String url = recommendationServiceUrl + "/api/recommend/stores";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (List<Map<String, Object>>) response.getBody().get("recommendations");
            }

            return Collections.emptyList();

        } catch (Exception e) {
            // 推荐服务失败时,降级为随机推荐
            System.err.println("[WARN] 推荐服务调用失败,使用降级策略: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 转换为DTO
     */
    private StoreRecommendationDTO convertToDTO(
            Map<String, Object> rec,
            int rank,
            Double userLat,
            Double userLng
    ) {
        StoreRecommendationDTO dto = new StoreRecommendationDTO();

        // 基本信息
        Long storeId = ((Number) rec.get("store_id")).longValue();
        Long listingId = ((Number) rec.get("listing_id")).longValue();

        dto.setStoreId(storeId);
        dto.setStoreName((String) rec.get("store_name"));
        dto.setListingId(listingId);
        dto.setTitle((String) rec.get("listing_title"));

        // 价格
        Double rescuePrice = ((Number) rec.get("rescue_price")).doubleValue();
        Double originalPrice = ((Number) rec.get("original_price")).doubleValue();
        dto.setRescuePrice(rescuePrice);
        dto.setOriginalPrice(originalPrice);

        // 计算折扣百分比: (originalPrice - rescuePrice) / originalPrice * 100
        int savingsPercentage = 0;
        if (originalPrice != null && originalPrice > 0 && rescuePrice != null) {
            savingsPercentage = (int) Math.round((originalPrice - rescuePrice) / originalPrice * 100);
        }
        dto.setSavingsPercentage(savingsPercentage);

        // 距离
        dto.setDistance(((Number) rec.get("distance")).doubleValue());

        // 评分
        Double avgRating = ((Number) rec.get("store_avg_rating")).doubleValue();
        dto.setAvgRating(avgRating);

        // 推荐分数
        dto.setPredictedScore(((Number) rec.get("predicted_score")).doubleValue());

        // 图片
        dto.setPhotoUrl((String) rec.get("photo_url"));

        // 从推荐服务返回的数据中获取字段（避免从实体类获取不存在的字段）
        dto.setCategory((String) rec.get("store_type"));

        // qty_available可能在Python服务返回的数据中不存在，使用null安全处理
        Object qtyObj = rec.get("qty_available");
        if (qtyObj != null) {
            dto.setQtyAvailable(((Number) qtyObj).intValue());
        } else {
            dto.setQtyAvailable(0);  // 默认值
        }

        // 从数据库获取Store的地址和坐标信息
        try {
            Store store = storeRepository.findById(storeId).orElse(null);
            if (store != null) {
                dto.setAddressLine(store.getAddressLine());
                dto.setLat(store.getLat() != null ? store.getLat().doubleValue() : null);
                dto.setLng(store.getLng() != null ? store.getLng().doubleValue() : null);
            }

            // 从数据库获取Listing的pickup时间
            Listing listing = listingRepository.findById(listingId).orElse(null);
            if (listing != null) {
                dto.setPickupStart(listing.getPickupStart() != null ? listing.getPickupStart().toString() : null);
                dto.setPickupEnd(listing.getPickupEnd() != null ? listing.getPickupEnd().toString() : null);
            }
        } catch (Exception e) {
            // 查询失败时忽略,使用已有信息
            System.err.println("[WARN] 获取详细信息失败: " + e.getMessage());
        }

        // 生成推荐原因
        dto.setRecommendationReason(generateRecommendationReason(dto, avgRating, savingsPercentage));

        return dto;
    }

    /**
     * Search with recommendations - Recommend products based on search keywords
     *
     * @param consumerId User ID
     * @param query      Search keyword
     * @param topK       Number of results to return (default 10)
     * @param userLat    User latitude (optional)
     * @param userLng    User longitude (optional)
     * @return ML-sorted search results
     */
    public List<StoreRecommendationDTO> searchWithRecommendations(
            Long consumerId,
            String query,
            Integer topK,
            Double userLat,
            Double userLng
    ) {
        if (topK == null) {
            topK = 10;
        }

        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Get all active listings
        List<Listing> activeListings = listingRepository.findByStatus("ACTIVE");

        // 2. Filter listings by keyword (title or description contains keyword)
        String searchQuery = query.toLowerCase().trim();
        List<Listing> matchedListings = activeListings.stream()
                .filter(listing -> {
                    String title = listing.getTitle() != null ? listing.getTitle().toLowerCase() : "";
                    String description = listing.getDescription() != null ? listing.getDescription().toLowerCase() : "";
                    return title.contains(searchQuery) || description.contains(searchQuery);
                })
                .collect(Collectors.toList());

        if (matchedListings.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Get user statistics
        ConsumerStats consumerStats = consumerStatsRepository
                .findByConsumerId(consumerId)
                .orElse(null);

        // 4. Build recommendation request (using matched listings as candidates)
        Map<String, Object> requestBody = buildRecommendationRequest(
                consumerId,
                matchedListings,
                consumerStats,
                userLat,
                userLng,
                topK
        );

        // 5. Call Python recommendation service for sorting
        List<Map<String, Object>> recommendations = callRecommendationService(requestBody);

        // 6. Convert to DTO
        List<StoreRecommendationDTO> result = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> rec : recommendations) {
            StoreRecommendationDTO dto = convertToDTO(rec, rank++, userLat, userLng);
            if (dto != null) {
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 生成推荐原因
     */
    private String generateRecommendationReason(StoreRecommendationDTO dto, Double avgRating, int savingsPercentage) {
        if (avgRating != null && avgRating >= 4.5) {
            return "High rating store";
        } else if (dto.getDistance() != null && dto.getDistance() <= 2.0) {
            return "Nearby location";
        } else if (savingsPercentage >= 60) {
            return "Great discount available";
        }
        return "Recommended for you";
    }

    /**
     * 计算两点间距离(Haversine公式)
     */
    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 999.0; // 默认很远
        }

        final int R = 6371; // 地球半径(km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
