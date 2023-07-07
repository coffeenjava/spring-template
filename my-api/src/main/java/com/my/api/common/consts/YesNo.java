package com.my.api.common.consts;

import com.fasterxml.jackson.annotation.JsonValue;
import com.my.api.common.model.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum YesNo implements BaseEnum {
    YES("Y"),
    NO("N"),
    NONE(null);

    @JsonValue
    private String code;

    public static YesNo byCode(String code) {
        return Arrays.stream(YesNo.values())
                .filter(e -> code.equals(e.getCode()))
                .findAny()
                .orElse(YesNo.NONE);
    }

    public static YesNo getBy(Boolean yesNo) {
        if (Objects.isNull(yesNo)) {
            return NONE;
        }
        return yesNo ? YES : NO;
    }

    public static YesNo getBy(boolean yn) {
        return yn ? YES : NO;
    }

    public boolean isTrue() {
        return this == YES;
    }

    public boolean isFalse() {
        return this == NO;
    }
}
