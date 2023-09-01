package com.my.api.common.model;

import java.io.Serializable;

/**
 * Enum 공통 컨버팅 적용을 위한 인터페이스
 */
public interface BaseEnum extends Serializable {
    Object getCode();

    static <S extends BaseEnum> S getEnum(Class<S> cls, Object code) {
        for (BaseEnum e : cls.getEnumConstants()) {
            if (code.equals(e.getCode())) return (S) e;
        }

        return null;
    }
}
