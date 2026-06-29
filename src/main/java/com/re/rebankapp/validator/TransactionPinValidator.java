package com.re.rebankapp.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TransactionPinValidator implements ConstraintValidator<TransactionPin, String> {

    private static final String PIN_PATTERN = "^[0-9]{6}$";

    @Override
    public boolean isValid(String pin, ConstraintValidatorContext context) {
        if (pin == null) {
            return true;
        }
        return pin.matches(PIN_PATTERN);
    }
}
