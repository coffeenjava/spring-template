package com.brian.api.common.event;

import com.brian.api.common.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

/**
 * 엔티티 이벤트(RequestScope) 저장소
 */
@RequestScope
@Component
public class EntityEventHolder {

    /**
     * 이벤트 목록
     */
    private LinkedList<EntityEventInfo> events = new LinkedList<>();

    /**
     * 이벤트 추가
     */
    public void addEntityEvent(EntityEventInfo e) {
        // 동일한 이벤트 + 동일 엔티티인 경우 이전 이벤트 정보 제거
        Optional<EntityEventInfo> duplicateEvent = events.stream()
                .filter(evt -> evt.isEqual(e))
                .findFirst();

        duplicateEvent.ifPresent(evt -> events.remove(evt));

        // 이벤트 추가
        events.add(e);
    }

    /**
     * 이벤트 추출
     */
    public EntityEventInfo popEntityEvent() {
        if (events.isEmpty()) return null;
        return events.removeFirst();
    }

    /**
     * 이벤트 정보
     */
    @Setter
    @Getter
    public static class EntityEventInfo<K,E> {
        private EntityEvent.Event event; // 이벤트 타입
        private Class<?> entityType; // 엔티티 클래스 타입
        private K id; // 엔티티 key
        private E entity; // 엔티티

        public EntityEventInfo(EntityEvent.Event event, K id, E entity) {
            this.event = event;
            this.id = id;
            this.entity = entity;
            this.entityType = entity.getClass();
        }

        public boolean isEqual(EntityEventInfo target) {
            if (event == target.getEvent()
                    && entityType == target.getEntityType()) {

                if (id != null && Objects.equals(id, target.getId())) return true;

                /**
                 * 트랜잭션 내에서 엔티티를 insert 한 후 update 수행될 경우
                 * insert 시에는 전처리이므로 엔티티의 id 가 null 이고, update 시에는 id 가 생성되어
                 * 동일 엔티티임에도 id 로 구분이 안되어 2번 수행되는 케이스가 발견됨.
                 * 엔티티 내의 id 도 비교하여 실제 동일 엔티티인지 한번 더 체크한다. (BaseEntity 를 상속한 엔티티에만 동작)
                 */
                if (entity instanceof BaseEntity) {
                    return ((BaseEntity) entity).getId().equals(((BaseEntity) target.entity).getId());
                }
            }

            return false;
        }
    }
}
