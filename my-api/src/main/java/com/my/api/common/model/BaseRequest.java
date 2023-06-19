package com.my.api.common.model;

import com.my.api.common.util.ObjectUtil;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * Controller 요청 객체 공통 interface
 *
 * @see com.my.api.common.interceptor.RequestValidationAspect
 */
public interface BaseRequest extends BaseModel {

    /**
     * dto 검증
     */
    default void validate() {}

    /**
     * 검증 전 초기화
     */
    default void beforeValidate() {
    }

    /**
     * 검증 후 초기화
     */
    default void afterValidate() {
    }

    /**
     * dto 초기화 + validation 호출
     */
    static void initAndValidate(final Object o) {
        if (o == null) return;

        if (o instanceof Collection) {
            final Collection dtos = (Collection) o;
            dtos.forEach(BaseRequest::initAndValidate);
        } else if (o instanceof BaseRequest) {
            BaseRequest request = (BaseRequest) o;
            // 검증 전 초기화
            request.beforeValidate();
            // 검증
            request.validate();
            // 검증 후 초기화
            request.afterValidate();

            // BaseRequest 구현체인 필드에 대해서도 검증 등 수행
            final Map<String, Field> fieldsMap = ObjectUtil.getAllFieldsMap(request.getClass());
            final BeanWrapper dtoWrapper = PropertyAccessorFactory.forBeanPropertyAccess(request);

            try {
                for (final Map.Entry<String, Field> entry : fieldsMap.entrySet()) {
                    final String fieldName = entry.getKey();
                    Object fieldValue;

                    if (dtoWrapper.isReadableProperty(fieldName)) {
                        fieldValue = dtoWrapper.getPropertyValue(fieldName);
                    } else {
                        final Field field = entry.getValue();
                        field.setAccessible(true);
                        fieldValue = field.get(request);
                    }

                    initAndValidate(fieldValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
