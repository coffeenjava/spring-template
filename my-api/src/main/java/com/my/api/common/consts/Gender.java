package com.my.api.common.consts;

import com.fasterxml.jackson.annotation.JsonValue;
import com.my.api.common.model.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 성별
 */
@Getter
@AllArgsConstructor
public enum Gender implements BaseEnum {
    MALE("M", "남자"),
    FEMALE("F", "여자");

    @JsonValue
    private String code;
    private String desc;
}
