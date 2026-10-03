package com.vestra.product.service.service.category;

import com.vestra.common.web.exception.ApiException;
import com.vestra.product.service.dto.category.CreateCategoryRequest;
import com.vestra.product.service.dto.category.UpdateCategoryRequest;
import com.vestra.product.service.entity.Category;
import com.vestra.product.service.projection.CategoryProjection;
import com.vestra.product.service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;


    public Category create(CreateCategoryRequest request){

        Category parent = request.parentId() != null && !request.parentId().isBlank()
                ? categoryRepository.findById(UUID.fromString(request.parentId()))
                .orElseThrow(() -> ApiException.badRequest("Üst kategori bulunamadı","Üst kategori bulunamadı"))
                : null;

        Category category = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .description(request.description())
                .parent(parent)
                .build();
        return this.categoryRepository.save(category);
    }

    public void update(UUID id, UpdateCategoryRequest request){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> ApiException.badRequest("Kategori bulunamadı","Kategori bulunamadı"));

        if (!category.getSlug().equals(request.slug()) && categoryRepository.existsBySlug(request.slug())) {
            throw ApiException.badRequest("Slug zaten kullanımda","Slug zaten kullanımda");
        }

        Category parent = request.parentId() != null && !request.parentId().isBlank()
                ? categoryRepository.findById(UUID.fromString(request.parentId()))
                .orElseThrow(() -> ApiException.badRequest("Üst kategori bulunamadı","Üst kategori bulunamadı"))
                : null;

        Category savedCategory = Category.builder()
                .id(id)
                .slug(request.slug())
                .description(request.description())
                .parent(parent)
                .name(request.name())
                .build();
        this.categoryRepository.save(savedCategory);
    }

    public void deleteById(UUID categoryId){
        this.categoryRepository.deleteById(categoryId);
    }

    public List<CategoryProjection> findAllDashboard(){
        return categoryRepository.findAllBy();
    }

}
