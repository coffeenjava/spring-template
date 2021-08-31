package com.brian.api.common.model;

import com.brian.api.common.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.lang.reflect.Field;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown=true)
public interface BaseRequest extends BaseModel {

    /**
     * dto 검증
     */
    default void validate() {}

    /**
     * dto 인스턴스 및 내부 dto 전체 validating
     */
    static void validate(Object o) {
        if (o == null) return;

        if (o instanceof Collection) {
            Collection dtos = (Collection) o;
            dtos.forEach(dto -> validate(dto));
        } else if (o instanceof BaseRequest){
            BaseRequest request = (BaseRequest) o;
            request.validate();

            Collection<Field> fields = ObjectUtil.getAllFields(request.getClass());
            BeanWrapper dtoWrapper = PropertyAccessorFactory.forBeanPropertyAccess(request);

            for (Field field : fields) {
                if (dtoWrapper.isReadableProperty(field.getName()) == false) continue;

                Object fo = dtoWrapper.getPropertyValue(field.getName());
                validate(fo);
            }
        }
    }
}
