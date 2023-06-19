package com.my.api.common.model;

import com.my.api.common.util.ObjectUtil;

import java.io.Serializable;

public interface BaseModel extends Serializable {
    default <T> T copyTo(T e) {
        // null 필드는 copy 에서 제외
        return ObjectUtil.copyProperties(this, e, false);
    }
}
