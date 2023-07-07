package com.my.api.common.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = NumberUnitValidator.class)
public @interface NumberUnit {

    /**
     * 설정 가능 단위
     */
    long value();

    String message() default "{com.my.api.common.validate.NumberUnit.message}";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
