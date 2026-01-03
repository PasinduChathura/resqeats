package com.ffms.resqeats.models.shop;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.model.AuditEntity;
import com.ffms.resqeats.enums.shop.ShopCategory;
import com.ffms.resqeats.enums.shop.ShopStatus;
import com.ffms.resqeats.models.usermgt.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop extends AuditEntity {

    @NotBlank
    @Column(name = "name", columnDefinition = "VARCHAR(150)", nullable = false)
    @JsonProperty("name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @NotBlank
    @Column(name = "address", columnDefinition = "VARCHAR(500)", nullable = false)
    @JsonProperty("address")
    private String address;

    @Column(name = "city", columnDefinition = "VARCHAR(100)")
    @JsonProperty("city")
    private String city;

    @Column(name = "postal_code", columnDefinition = "VARCHAR(20)")
    @JsonProperty("postal_code")
    private String postalCode;

    @NotNull
    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    @JsonProperty("latitude")
    private BigDecimal latitude;

    @NotNull
    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    @JsonProperty("longitude")
    private BigDecimal longitude;

    @Column(name = "phone", columnDefinition = "VARCHAR(20)")
    @JsonProperty("phone")
    private String phone;

    @Column(name = "email", columnDefinition = "VARCHAR(100)")
    @JsonProperty("email")
    private String email;

    @Column(name = "image_url", columnDefinition = "VARCHAR(500)")
    @JsonProperty("image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", columnDefinition = "VARCHAR(30)")
    @JsonProperty("category")
    private ShopCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(30)")
    @JsonProperty("status")
    @Builder.Default
    private ShopStatus status = ShopStatus.PENDING_APPROVAL;

    @Column(name = "opening_time")
    @JsonProperty("opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    @JsonProperty("closing_time")
    private LocalTime closingTime;

    @Column(name = "pickup_start_time")
    @JsonProperty("pickup_start_time")
    private LocalTime pickupStartTime;

    @Column(name = "pickup_end_time")
    @JsonProperty("pickup_end_time")
    private LocalTime pickupEndTime;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @JsonProperty("average_rating")
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_ratings")
    @JsonProperty("total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "is_open")
    @JsonProperty("is_open")
    @Builder.Default
    private Boolean isOpen = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private Set<ShopOperatingDay> operatingDays = new HashSet<>();
}
