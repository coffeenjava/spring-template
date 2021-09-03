package com.brian.api.common.event;

import lombok.AllArgsConstructor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이벤트
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EntityEvent {

    Event[] value();

    /**
     * 수행할 이벤트
     */
    @AllArgsConstructor
    enum Event {
        PRODUCT_CHANGE("상품 생성/변경"),
        ;

        private String desc;
    }
}