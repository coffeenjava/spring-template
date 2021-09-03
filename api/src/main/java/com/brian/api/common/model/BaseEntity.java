package com.brian.api.common.model;

public interface BaseEntity extends BaseModel {

    default void setCreator(Integer id) {}
    default void setUpdater(Integer id) {}

    /**
     * 엔티티 이벤트 처리시 중복 확인을 위해 키를 추출하기 위한 용도
     */
    default Object getId() { return new Object(); }

    /**
     * 등록자 설정
     */
    static void setCreator(BaseEntity e) {
    }

    /**
     * 수정자 설정
     */
    static void setUpdater(BaseEntity e) {
    }
}