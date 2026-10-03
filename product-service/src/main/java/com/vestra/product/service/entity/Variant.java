package com.vestra.product.service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "variants")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Variant implements Serializable {
    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @Transient
    private int initialStock;
    @Column(precision = 10,scale = 2)
    private BigDecimal price;
    @Column(unique = true,nullable = false)
    private String sku;
    @OneToMany(mappedBy = "variant",cascade = CascadeType.ALL,orphanRemoval = true)
    @Builder.Default
    private Set<VariantAttribute> attributes = new HashSet<>();
}
