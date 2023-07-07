package com.my.api.common.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = MaxByteValidator.class)
public @interface MaxByte {

    /**
     * 최대 byte 제한
     */
    int value();

    String message() default "{com.my.api.common.validate.MaxByte.message}";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
