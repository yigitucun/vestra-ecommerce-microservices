package com.vestra.product.service.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = UniqueProductSlugValidator.class)
public @interface UniqueProductSlug {
    String message() default "Slug adresi önceden alınmış.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
