package com.frh.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐商家响应DTO
 * 用于Android首页显示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRecommendationDTO {

    /**
     * 商品信息
     */
    private Long listingId;
    private String title;
    private Double originalPrice;
    private Double rescuePrice;
    private Integer savingsPercentage;  // 折扣百分比 (original - rescue) / original * 100
    private String pickupStart;
    private String pickupEnd;
    private String photoUrl;
    private Integer qtyAvailable;

    /**
     * 商家信息
     */
    private Long storeId;
    private String storeName;
    private String category;  // store type
    private String addressLine;
    private Double lat;
    private Double lng;

    /**
     * 计算字段
     */
    private Double distance;  // 距离用户的距离(km)
    private Double avgRating;  // 平均评分

    /**
     * 推荐上下文
     */
    private Double predictedScore;  // ML模型预测分数
    private String recommendationReason;  // 推荐原因
}
