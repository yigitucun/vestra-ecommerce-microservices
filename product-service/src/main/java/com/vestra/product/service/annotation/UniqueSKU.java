package com.vestra.product.service.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueSKUValidator.class)
public @interface UniqueSKU {
    String message() default "SKU daha önceden alınmış.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
