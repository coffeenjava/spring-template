package com.brian.api.common.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = LocalDateTypeValidator.class)
public @interface LocalDateType {

    /**
     * 여러 개의 날짜인 경우 구분자
     */
    String splitter() default ",";

    /**
     * 날짜 형식
     */
    String format() default "yyyy-MM-dd";

    String message() default "{com.brian.api.common.validate.LocalDateType.message}";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
