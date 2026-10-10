package com.vestra.product.service.controller;

import com.vestra.product.service.dto.variant.VariantInfoResponse;
import com.vestra.product.service.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ){
        return ResponseEntity.ok(productService.getAll(page,size));
    }

    @GetMapping("/variants/{variantId}")
    public ResponseEntity<VariantInfoResponse> getVariant(@PathVariable UUID variantId) {
        return ResponseEntity.ok(productService.getVariantInfo(variantId));
    }

    @PostMapping("/variants/batch")
    public ResponseEntity<List<VariantInfoResponse>> getVariantsBatch(@RequestBody List<UUID> variantIds) {
        return ResponseEntity.ok(productService.getVariantsInfo(variantIds));
    }
}
