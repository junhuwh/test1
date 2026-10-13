package com.bookworm.entity;

import com.bookworm.entity.enums.BookFormat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "author_name", nullable = false, length = 150)
    private String authorName;

    @Column(name = "author_bio", columnDefinition = "TEXT")
    private String authorBio;

    @Column(name = "author_avatar_url", length = 500)
    private String authorAvatarUrl;

    @Column(name = "publisher_name", nullable = false, length = 150)
    @Builder.Default
    private String publisherName = "ABC Publishers";

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 50)
    @Builder.Default
    private BookFormat format = BookFormat.Paperback;

    @Column(name = "language", nullable = false, length = 50)
    @Builder.Default
    private String language = "English";

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 50;

    @Column(name = "sales_count", nullable = false)
    @Builder.Default
    private Integer salesCount = 0;

    @Column(name = "rating", nullable = false, precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal rating = new BigDecimal("4.5");

    @Column(name = "front_cover_url", length = 500)
    private String frontCoverUrl;

    @Column(name = "back_cover_url", length = 500)
    private String backCoverUrl;

    @Column(name = "is_bestseller", nullable = false)
    @Builder.Default
    private Boolean isBestseller = false;

    @Column(name = "is_new_launch", nullable = false)
    @Builder.Default
    private Boolean isNewLaunch = false;

    @Column(name = "published_date", nullable = false)
    @Builder.Default
    private LocalDate publishedDate = LocalDate.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorBio() { return authorBio; }
    public void setAuthorBio(String authorBio) { this.authorBio = authorBio; }
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public void setAuthorAvatarUrl(String authorAvatarUrl) { this.authorAvatarUrl = authorAvatarUrl; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public BookFormat getFormat() { return format; }
    public void setFormat(BookFormat format) { this.format = format; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Integer getSalesCount() { return salesCount; }
    public void setSalesCount(Integer salesCount) { this.salesCount = salesCount; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public String getFrontCoverUrl() { return frontCoverUrl; }
    public void setFrontCoverUrl(String frontCoverUrl) { this.frontCoverUrl = frontCoverUrl; }
    public String getBackCoverUrl() { return backCoverUrl; }
    public void setBackCoverUrl(String backCoverUrl) { this.backCoverUrl = backCoverUrl; }
    public Boolean getIsBestseller() { return isBestseller; }
    public void setIsBestseller(Boolean isBestseller) { this.isBestseller = isBestseller; }
    public Boolean getIsNewLaunch() { return isNewLaunch; }
    public void setIsNewLaunch(Boolean isNewLaunch) { this.isNewLaunch = isNewLaunch; }
    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }
}
