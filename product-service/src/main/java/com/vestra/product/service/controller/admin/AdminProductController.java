package com.vestra.product.service.controller.admin;

import com.vestra.product.service.dto.product.CreateProductRequest;
import com.vestra.product.service.dto.product.UpdateProductRequest;
import com.vestra.product.service.service.product.ProductService;
import com.vestra.product.service.service.product.RegisterProductService;
import com.vestra.product.service.service.product.UpdateProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final RegisterProductService registerProductService;
    private final UpdateProductService updateProductService;
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> registerProduct(@Valid @RequestBody CreateProductRequest request){
        registerProductService.execute(request);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request){
        updateProductService.execute(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id){
        productService.delete(id);
        return ResponseEntity.status(204).build();
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ){
        return ResponseEntity.ok(productService.getAll(page,size));
    }
}
