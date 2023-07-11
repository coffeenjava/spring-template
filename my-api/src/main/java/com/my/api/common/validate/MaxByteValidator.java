package com.my.api.common.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

/**
 * 지정된 byte 초과 여부 검증 용도
 */
public class MaxByteValidator implements ConstraintValidator<MaxByte, String> {

    private int max;

    @Override
    public void initialize(MaxByte maxByte) {
        max = maxByte.value();
    }

    @Override
    public boolean isValid(String inputValue, ConstraintValidatorContext context) {
        return inputValue == null || inputValue.getBytes(StandardCharsets.UTF_8).length <= max;
    }
}
