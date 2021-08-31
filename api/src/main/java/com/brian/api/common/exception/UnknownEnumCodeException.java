package com.brian.api.common.exception;

public class UnknownEnumCodeException extends RuntimeException {
    public UnknownEnumCodeException(Class cls, Object code) {
        super("UnknownEnumCodeException : Enum code value -> ["+code+"]("+code.getClass().getName()+") not in "+cls.getName());
    }
}
