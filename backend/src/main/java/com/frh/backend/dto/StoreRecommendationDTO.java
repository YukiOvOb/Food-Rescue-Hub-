package com.frh.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 推荐商家响应DTO
 * 用于Android首页显示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRecommendationDTO {

    /**
     * 商家信息
     */
    private Long storeId;
    private String storeName;
    private String storeType;
    private String address;
    private Double lat;
    private Double lng;
    private Double distance;  // 距离用户的距离(km)

    /**
     * 推荐的商品信息(该店铺最匹配的商品)
     */
    private Long listingId;
    private String listingTitle;
    private BigDecimal rescuePrice;
    private BigDecimal originalPrice;
    private Double discountRate;
    private String photoUrl;  // 商品图片URL

    /**
     * 商家统计信息
     */
    private BigDecimal avgRating;     // 平均评分
    private Integer totalOrders;       // 总订单数
    private BigDecimal completionRate; // 完成率

    /**
     * 推荐分数(由ML模型计算)
     */
    private Double predictedScore;
    private Integer rank;  // 推荐排名(1-5)

    /**
     * 显示标签
     */
    private String tag;  // 例如: "高评分", "附近", "热门"
}
