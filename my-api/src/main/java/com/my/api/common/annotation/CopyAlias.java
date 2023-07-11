package com.my.api.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 필드 copy 대상 추가 설정 용도
 * <p>이름이 다른 필드에 값을 복사할 경우 사용
 *
 * @see com.my.api.common.util.ObjectUtil#copyProperties(Object, Object, boolean)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface CopyAlias {
    String[] value();
}
