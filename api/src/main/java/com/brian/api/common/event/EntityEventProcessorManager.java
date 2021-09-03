package com.brian.api.common.event;

import com.brian.api.common.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 엔티티 이벤트 일괄 처리기
 */
@RequiredArgsConstructor
@Component
public class EntityEventProcessorManager {

    private final Set<EntityEventProcessor> processors;

    /**
     * 이벤트 일괄 수행
     */
    public void process() {
        // 이벤트 정보 가져오기
        EntityEventHolder eventHolder = ObjectUtil.getBean(EntityEventHolder.class);

        // 이벤트 수행 중 새로운 이벤트가 holder 에 추가될 수 있으므로
        // 더이상 추출할 이벤트가 없을때까지 반복
        while(true) {
            EntityEventHolder.EntityEventInfo e = eventHolder.popEntityEvent();

            if (e == null) break;

            processors.stream()
                    .filter(p -> p.supports(e))
                    .forEach(p -> p.process(e));
        }
    }
}
