package com.brian.api.common.interceptor.jpa;

import com.brian.api.common.event.EntityEvent;
import com.brian.api.common.event.EntityEventHolder;
import com.brian.api.common.event.EntityEventProcessorManager;
import com.brian.api.common.util.ObjectUtil;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * 엔티티 변경 감지 인터셉터
 */
public class EntityChangeInterceptor extends EmptyInterceptor {

    /**
     * 조회 후처리
     */
    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        return super.onLoad(entity, id, state, propertyNames, types);
    }

    /**
     * insert 전처리
     * 여기서 값을 변경하면 insert 쿼리 실행 후 update(onFlushDirty) 동작
     */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        /**
         * 엔티티 생성 이벤트 처리
         */
        EntityEvent entityEvent = entity.getClass().getAnnotation(EntityEvent.class);

        //
        if (entityEvent == null) {
            entityEvent = entity.getClass().getSuperclass().getAnnotation(EntityEvent.class);
        }

        if (entityEvent != null) {
            EntityEventHolder eventHolder = ObjectUtil.getBean(EntityEventHolder.class);
            EntityEvent.Event[] events = entityEvent.value();

            for (EntityEvent.Event event : events) {
                EntityEventHolder.EntityEventInfo info = new EntityEventHolder.EntityEventInfo(event, id, entity);
                eventHolder.addEntityEvent(info);
            }
        }
        return super.onSave(entity, id, state, propertyNames, types);
    }

    /**
     * flush 전처리
     * 1차 캐시의 모든 엔티티 대상
     */
    @Override
    public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        return super.findDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    /**
     * 각 엔티티 별로 findDirty 이후 동작
     * 변경된 엔티티 대상
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        // Todo 변경이 있어 실제로 flush 될 엔티티가 넘어오므로
        // Todo Updater 설정 등 엔티티에 자동으로 설정 해줄 행위들은 이곳에서 하면 좋다.

        /**
         * 필드별 이벤트 처리
         */
        // 이벤트 설정된 필드 전체 조회
        Map<String,Field> eventFieldMap = ObjectUtil.getAnnotatedFieldMap(entity.getClass(), EntityEvent.class);

        // 이벤트 설정된 필드에 변경이 있으면 EventHolder 에 저장
        EntityEventHolder eventHolder = ObjectUtil.getBean(EntityEventHolder.class);

        for (int i=0; i<propertyNames.length; i++) {
            // 이벤트 정의된 필드가 아니면 continue
            Field eventField = eventFieldMap.get(propertyNames[i]);
            if (eventField == null) continue;

            // 변경이 없으면 continue
            if (Objects.equals(currentState[i], previousState[i])) continue;

            EntityEvent entityEvent = eventField.getAnnotation(EntityEvent.class);
            EntityEvent.Event[] events = entityEvent.value();

            for (EntityEvent.Event event : events) {
                EntityEventHolder.EntityEventInfo info = new EntityEventHolder.EntityEventInfo(event, id, entity);
                eventHolder.addEntityEvent(info);
            }
        }

        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    /**
     * flush 후처리
     * 1차 캐시의 모든 엔티티 대상
     */
    @Override
    public void postFlush(Iterator entities) {
//        System.out.println("##### post flush start #####");
//        entities.forEachRemaining(e -> {
//            System.out.println(e.getClass().getName());
//        });
//        System.out.println("##### post flush finished #####");
        super.postFlush(entities);
    }

    /**
     * Transaction commit 전처리
     */
    @Override
    public void beforeTransactionCompletion(Transaction tx) {
//        System.out.println("##### transaction complete -> " + tx.getStatus());

        /**
         * 엔티티 이벤트 수행
         */
        EntityEventProcessorManager eventManager = ObjectUtil.getBean(EntityEventProcessorManager.class);
        eventManager.process();

        super.beforeTransactionCompletion(tx);
    }
}
