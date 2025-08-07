package com.example.complexapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_price", columnList = "price"),
    @Index(name = "idx_product_sku", columnList = "sku")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Column(unique = true, nullable = false)
    private String sku;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @NotNull(message = "Stock quantity is required")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 10;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel = 1000;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type = ProductType.PHYSICAL;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "dimensions_cm")
    private String dimensionsCm;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    @Column(name = "is_featured")
    private boolean featured = false;

    @Column(name = "is_bestseller")
    private boolean bestseller = false;

    @Column(name = "rating_average")
    private Double ratingAverage = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "sold_count")
    private Long soldCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_tags",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProductImage> images = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProductReview> reviews = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrderItem> orderItems = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum ProductStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED
    }

    public enum ProductType {
        PHYSICAL, DIGITAL, SERVICE, SUBSCRIPTION
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isInStock() {
        return stockQuantity > 0 && status == ProductStatus.ACTIVE;
    }

    public boolean isLowStock() {
        return stockQuantity <= minStockLevel;
    }

    public BigDecimal getDiscountPercentage() {
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            return originalPrice.subtract(price)
                    .divide(originalPrice, 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementSoldCount(int quantity) {
        this.soldCount += quantity;
    }

    public void updateRating(double newRating) {
        if (ratingCount == 0) {
            ratingAverage = newRating;
        } else {
            double totalRating = ratingAverage * ratingCount + newRating;
            ratingCount++;
            ratingAverage = totalRating / ratingCount;
        }
    }
}
