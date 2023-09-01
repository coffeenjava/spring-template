package com.my.api.common.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 지정된 단위의 숫자인지 검증 용도
 */
public class NumberUnitValidator implements ConstraintValidator<NumberUnit, Number> {

    private long unit;

    @Override
    public void initialize(NumberUnit unit) {
        this.unit = unit.value();
    }

    @Override
    public boolean isValid(Number inputValue, ConstraintValidatorContext context) {
        return inputValue == null || inputValue.longValue() % unit == 0;
    }
}
