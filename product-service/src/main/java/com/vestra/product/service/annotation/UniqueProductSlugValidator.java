package com.vestra.product.service.annotation;

import com.vestra.product.service.repository.ProductRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UniqueProductSlugValidator implements ConstraintValidator<UniqueProductSlug,String> {

    private final ProductRepository productRepository;

    @Override
    public boolean isValid(String string, ConstraintValidatorContext constraintValidatorContext) {
        return !productRepository.existsBySlug(string);
    }

}
