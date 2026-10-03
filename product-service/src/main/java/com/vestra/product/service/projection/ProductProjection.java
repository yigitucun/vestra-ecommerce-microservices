package com.vestra.product.service.projection;


import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProductProjection {
    UUID getId();
    String getName();
    String getDescription();
    String getSlug();
    String getImageUrl();
    CategoryInfo getCategory();
    List<VariantInfo> getVariants();

    interface VariantInfo {
        UUID getId();
        BigDecimal getPrice();
        String getSku();
        Set<VariantAttributeInfo> getAttributes();
    }

    interface VariantAttributeInfo {
        UUID getId();
        String getAttributeName();
        String getAttributeValue();
    }

    interface CategoryInfo {
        UUID getId();
        String getName();
        String getSlug();
    }
}
