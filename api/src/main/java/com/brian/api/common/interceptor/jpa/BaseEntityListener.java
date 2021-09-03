package com.brian.api.common.interceptor.jpa;


import com.brian.api.common.model.BaseEntity;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * BaseEntity 상속한 엔티티의 변경 감지 Listener
 *
 * 적용할 Entity 클래스에 @EntityListeners(EntityListener.class) 설정 필요
 */
public class BaseEntityListener {

    /**
     * 생성 flush 전처리
     */
    @PrePersist
    public void onPrePersist(Object target) {
        BaseEntity.setCreator((BaseEntity) target);
    }

    /**
     * 수정 flush 전처리
     */
    @PreUpdate
    public void onPreUpdate(Object target) {
        BaseEntity.setUpdater((BaseEntity) target);
    }
}
