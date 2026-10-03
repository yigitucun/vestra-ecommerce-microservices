package com.vestra.item.service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "items")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Item {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID variantId;
    @Builder.Default
    private int quantity = 0;
    @Builder.Default
    private int reserved = 0;
    @Builder.Default
    private boolean active=true;
    @UpdateTimestamp
    private Instant updatedAt;
}
