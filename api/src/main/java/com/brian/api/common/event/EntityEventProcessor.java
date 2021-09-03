package com.brian.api.common.event;

/**
 * 엔티티 이벤트 수행 인터페이스
 */
public interface EntityEventProcessor {
    /**
     * 수행할 이벤트인지 여부
     */
    default boolean supports(EntityEventHolder.EntityEventInfo eventInfo) {
        return true;
    }

    /**
     * 이벤트 수행
     */
    void process(EntityEventHolder.EntityEventInfo eventInfo);
}
