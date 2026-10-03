package com.vestra.product.service.annotation;


import com.vestra.product.service.repository.VariantRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueSKUValidator implements ConstraintValidator<UniqueSKU,String> {

    private final VariantRepository variantRepository;

    @Override
    public boolean isValid(String string, ConstraintValidatorContext constraintValidatorContext) {
        return !variantRepository.existsBySku(string);
    }
}
