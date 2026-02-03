package com.frh.backend.controller;

import com.frh.backend.dto.StoreRecommendationDTO;
import com.frh.backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推荐系统API控制器
 * 为Android应用提供个性化商家推荐
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")  // 允许跨域(生产环境应限制具体域名)
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 获取首页推荐商家
     *
     * GET /api/recommendations/homepage?consumerId=1&topK=5&lat=1.3521&lng=103.8198
     *
     * @param consumerId 用户ID
     * @param topK       返回前K个商家(默认5)
     * @param lat        用户纬度(可选)
     * @param lng        用户经度(可选)
     * @return 推荐的商家列表
     */
    @GetMapping("/homepage")
    public ResponseEntity<Map<String, Object>> getHomepageRecommendations(
            @RequestParam Long consumerId,
            @RequestParam(required = false, defaultValue = "5") Integer topK,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        try {
            List<StoreRecommendationDTO> recommendations =
                    recommendationService.recommendStoresForHomepage(
                            consumerId,
                            topK,
                            lat,
                            lng
                    );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("consumerId", consumerId);
            response.put("recommendations", recommendations);
            response.put("count", recommendations.size());
            response.put("message", "推荐获取成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "推荐获取失败");

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Search with recommendations - Recommend products based on search keywords
     *
     * GET /api/recommendations/search?consumerId=1&query=bread&topK=10&lat=1.3521&lng=103.8198
     *
     * @param consumerId User ID
     * @param query      Search keyword
     * @param topK       Maximum number of results (optional, null = return all)
     * @param lat        User latitude (optional)
     * @param lng        User longitude (optional)
     * @return ML-sorted search results
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchWithRecommendations(
            @RequestParam Long consumerId,
            @RequestParam String query,
            @RequestParam(required = false) Integer topK,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        try {
            List<StoreRecommendationDTO> recommendations =
                    recommendationService.searchWithRecommendations(
                            consumerId,
                            query,
                            topK,
                            lat,
                            lng
                    );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("consumerId", consumerId);
            response.put("query", query);
            response.put("recommendations", recommendations);
            response.put("count", recommendations.size());
            response.put("message", "Search recommendations retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Failed to retrieve search recommendations");

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 健康检查
     *
     * GET /api/recommendations/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Recommendation API");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
