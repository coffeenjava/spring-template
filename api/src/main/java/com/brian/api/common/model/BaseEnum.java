package com.brian.api.common.model;

import com.brian.api.common.annotation.Description;
import com.brian.api.common.exception.UnknownEnumCodeException;

@Description(value = "커스텀 Enum 인터페이스", comment = "Enum 공통 컨버팅 적용을 위한 인터페이스")
public interface BaseEnum {
    Object getCode();

    static <S extends BaseEnum> S getEnum(Class<S> cls, Object code) {
        for (BaseEnum e : cls.getEnumConstants()) {
            if (code.equals(e.getCode())) return (S) e;
        }
        
        throw new UnknownEnumCodeException(cls, code);
    }
}
