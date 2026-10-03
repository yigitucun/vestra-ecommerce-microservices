package com.vestra.product.service.controller.admin;

import com.vestra.product.service.dto.category.CreateCategoryRequest;
import com.vestra.product.service.dto.category.UpdateCategoryRequest;
import com.vestra.product.service.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok().body(categoryService.findAllDashboard());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateCategoryRequest request){
        return ResponseEntity.status(201)
                .body(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateCategoryRequest request, @PathVariable UUID id){
        categoryService.update(id,request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        categoryService.deleteById(id);
        return ResponseEntity.status(204).build();
    }
}
