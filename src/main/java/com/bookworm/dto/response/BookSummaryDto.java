package com.bookworm.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSummaryDto {
    private UUID id;
    private String title;
    private String authorName;
    private BigDecimal price;
    private String format;
    private String categoryName;
    private BigDecimal rating;
    private String frontCoverUrl;
    private String estimatedDelivery;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public String getFrontCoverUrl() { return frontCoverUrl; }
    public void setFrontCoverUrl(String frontCoverUrl) { this.frontCoverUrl = frontCoverUrl; }
    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
}
