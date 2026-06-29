package com.re.rebankapp.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TransactionPinValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionPin {
    String message() default "Mã PIN phải bao gồm đúng 6 chữ số";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
