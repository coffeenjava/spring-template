package com.brian.api.common.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * formatter 형식의 날짜 문자열인지 검증 용도
 */
public class LocalDateTypeValidator implements ConstraintValidator<LocalDateType, String> {

    private DateTimeFormatter formatter;
    private String splittter;

    @Override
    public void initialize(LocalDateType localDateType) {
        splittter = localDateType.splitter();
        formatter = DateTimeFormatter.ofPattern(localDateType.format());
    }

    @Override
    public boolean isValid(String inputValue, ConstraintValidatorContext context) {
        if (inputValue == null) return true;

        try {
            String[] dates = inputValue.split(splittter);

            for (String date : dates) {
                LocalDate.parse(date, formatter);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
