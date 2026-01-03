package com.ffms.resqeats.models.food;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.model.AuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "secret_box_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecretBoxItem extends AuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secret_box_id", nullable = false)
    @JsonBackReference
    private SecretBox secretBox;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @Column(name = "quantity")
    @JsonProperty("quantity")
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "notes", columnDefinition = "VARCHAR(500)")
    @JsonProperty("notes")
    private String notes;
}
