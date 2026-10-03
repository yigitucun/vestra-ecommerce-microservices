package com.vestra.product.service.projection;

import java.util.List;
import java.util.UUID;

public interface CategoryProjection {
    UUID getId();
    String getName();
    String getSlug();
    List<Parent> getParent();

    interface Parent {
        UUID getId();
        String getName();
    }
}
