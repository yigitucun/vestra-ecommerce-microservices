package com.vestra.product.service.annotation;

import com.vestra.product.service.repository.CategoryRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueCategorySlugValidator implements ConstraintValidator<UniqueCategorySlug,String> {

    private final CategoryRepository categoryRepository;

    @Override
    public boolean isValid(String string, ConstraintValidatorContext constraintValidatorContext) {
        return !categoryRepository.existsBySlug(string);
    }
}
