package com.vestra.product.service.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueCategorySlugValidator.class)
public @interface UniqueCategorySlug {
    String message() default "Slug adresi önceden girilmiş.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
