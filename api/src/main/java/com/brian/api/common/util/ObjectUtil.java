package com.brian.api.common.util;

import com.brian.api.common.annotation.CopyAlias;
import com.brian.api.common.annotation.Description;
import com.brian.api.common.annotation.NoCopy;
import com.brian.api.common.annotation.SkipNull;
import com.brian.api.common.model.BaseEnum;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ObjectUtil implements ApplicationContextAware {

	private static ApplicationContext ac;

	@Override
	public void setApplicationContext(ApplicationContext ac) {
		this.ac = ac;
	}

	public static <T> T getBean(Class<T> beanClass) {
		return ac.getBean(beanClass);
	}

	public static <T> T copyProperties(Object source, T target) throws BeansException {
		return copyProperties(source, target, true);
	}

	/**
	 * copy properties
	 *
	 * getter/setter 가 있을 경우 동작
	 * NoCopy 어노테이션 선언된 필드 제외
	 * Map, Entity, Entity Collection(AbstractPersistentCollection) 이면 제외
	 * 	- Entity 내부의 Entity 는 복사하지 않도록 함
	 * CopyAlias 어노테이션 선언된 필드에도 copy
	 *
	 * @param source	원본
	 * @param target	대상
	 * @param includeNull	null 필드 포함 여부
	 *                      true 인 경우 null 필드도 copy (SkipNull 어노테이션이 선언된 필드는 제외)
	 */
	public static <T> T copyProperties(Object source, T target, boolean includeNull) throws BeansException {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");

		Map<String,String> fieldNameMap = new HashMap<>(); // target field,source field
		Set<String> skipNullFieldSet = new HashSet<>();

		for (Field field : getAllFields(source.getClass())) {
			NoCopy noCopy = field.getAnnotation(NoCopy.class);

			boolean isCopy = true;
			if (noCopy != null) {
				for (Class noCopyTarget : noCopy.value()) {
					if (noCopyTarget == Void.class || noCopyTarget == target.getClass()) {
						isCopy = false;
						break;
					}
				}
			}

			if (isCopy) fieldNameMap.put(field.getName(),field.getName());

			CopyAlias copyAlias = field.getAnnotation(CopyAlias.class);
			if (copyAlias != null) {
				for (String alias : copyAlias.value()) {
					fieldNameMap.put(alias, field.getName());
				}
			}

			SkipNull skipNull = field.getAnnotation(SkipNull.class);
			boolean isSkipNull = false;
			if (field.getAnnotation(SkipNull.class) != null) {
				for (Class skipNullClass : skipNull.value()) {
					if (skipNullClass == Void.class || skipNullClass == target.getClass()) {
						isSkipNull = true;
						break;
					}
				}
			}
			if (isSkipNull) skipNullFieldSet.add(field.getName());
		}

		BeanWrapper sourceWrapper = PropertyAccessorFactory.forBeanPropertyAccess(source);
		BeanWrapper targetWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);

		for (String targetName : fieldNameMap.keySet()) {
			// getter/setter 체크
			if (sourceWrapper.isReadableProperty(targetName) == false
					|| targetWrapper.isWritableProperty(targetName) == false) continue;

			String sourceName = fieldNameMap.get(targetName);
			Object value = sourceWrapper.getPropertyValue(sourceName);

			// 값이 null 인 경우
			if (value == null) {
				if (includeNull == false || skipNullFieldSet.contains(sourceName)) continue;
			}

			// Map, Entity 이면 제외
			if (value != null && (value instanceof Map || isEntity(value))) continue;

			// Entity Collection 이면 제외
			// 내부 값에 접근하면 Lazy 엔티티에 대한 조회가 발생하므로 여기서 제외한다.
			if (value instanceof AbstractPersistentCollection) continue;

			// 원본, 타켓 둘 다 Collection 인 경우
			if (value instanceof Collection && Collection.class.isAssignableFrom(targetWrapper.getPropertyType(targetName))) {
				Collection collectionValue = (Collection) value;
				if (collectionValue.size() == 0) continue;

				Class sourceType = collectionValue.iterator().next().getClass();
				Class targetType = getGenericTypeFromCollection(target.getClass(), targetName);

				if (sourceType != targetType) continue; // 동일 구현 타입이 아닌 경우 제외

				Collection newCollection = createInstance(collectionValue.getClass()); // 원본 값 타입으로 생성

				if (BeanUtils.isSimpleValueType(sourceType)) {
					newCollection.addAll(collectionValue);
				} else {
					collectionValue.forEach(e -> {
						Object targetElement = createInstance(sourceType);
						copyProperties(e, targetElement, includeNull);
						newCollection.add(targetElement);
					});
				}

				targetWrapper.setPropertyValue(targetName, newCollection);
			} else {
				targetWrapper.setPropertyValue(targetName, value);
			}
		}

		return target;
	}

	public static boolean isEntity(Object o) {
		return isEntity(o.getClass());
	}

	public static boolean isEntity(Class cls) {
		return AnnotatedElementUtils.hasAnnotation(cls, Entity.class);
	}

	public static <T> T createInstance(Class<T> cls) {
		try {
			return cls.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 클래스의 모든 필드 가져오기
	 *
	 * 상속받은 필드 포함
	 * 중복 선언된 필드의 경우 하위 우선
	 */
	public static Collection<Field> getAllFields(Class<?> type) {
		Map<String,Field> fieldMap = new HashMap<>();

		ReflectionUtils.doWithFields(type, field -> {
			if (fieldMap.containsKey(field.getName()) == false) {
				fieldMap.put(field.getName(), field);
			}
		});

		return fieldMap.values();
	}

	/**
     * 클래스의 Simple 타입 필드 가져오기
     */
	public static Collection<Field> getSimpleTypeFields(Class<?> type) {
		Map<String,Field> fieldMap = new HashMap<>();

		ReflectionUtils.doWithFields(type, field -> {
			if (fieldMap.containsKey(field.getName()) == false
					&& BeanUtils.isSimpleValueType(field.getType())) {
				fieldMap.put(field.getName(), field);
			}
		});

		return fieldMap.values();
	}

	/**
	 * null 인 필드 목록 가져오기
	 */
	public static List<String> getNullFieldNames(Object o) {
		List<String> fields = new ArrayList<>();
		ReflectionUtils.doWithFields(o.getClass(), field -> {
			field.setAccessible(true);
			Object v = field.get(o);
			if (v == null) fields.add(field.getName());
		});

		return fields;
	}

	/**
	 * null 아닌 필드 목록 가져오기
	 */
	public static List<String> getNonNullFieldNames(Object o) {
		List<String> fields = new ArrayList<>();
		ReflectionUtils.doWithFields(o.getClass(), field -> {
			field.setAccessible(true);
			Object v = field.get(o);
			if (v != null) fields.add(field.getName());
		});

		return fields;
	}

	/**
	 * Null 인 필드 존재 체크
	 */
	public static boolean hasNullFields(Object o) {
		return getNullFieldNames(o).size() > 0;
	}

	/**
	 * 모든 필드가 Null 인지 체크
	 */
	public static boolean isAllFieldsNull(Object o) {
		return getNonNullFieldNames(o).size() == 0;
	}

	/**
	 * 필드의 @Description value 가져오기
	 */
	public static String getDescription(Class c, String fieldName) {
		String desc = fieldName;
		try {
			Field f = c.getDeclaredField(fieldName);
			Description anno = f.getAnnotation(Description.class);
			if (anno != null) desc = anno.value();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		return "["+desc+"] ";
	}

	/**
	 * Collection 의 Generic 타입 가져오기
	 *
	 * @param model       	Collection 이 정의된 model 클래스
	 * @param fieldName		Collection 필드명
	 */
	public static Class<?> getGenericTypeFromCollection(Class model, String fieldName) {
		try {
			return (Class<?>) ((ParameterizedType) model.getDeclaredField(fieldName).getGenericType()).getActualTypeArguments()[0];
		} catch (NoSuchFieldException e) {}

		return null;
	}

	/**
	 * 값이 다른 필드 가져오기
	 * source 나 target 이 null 이면 empty list 반환
	 */
	public static <T> List<Field> getDiffFields(T source, T target) {
		List<Field> diff = new ArrayList<>();
		if (source == null || target == null) return diff;

		Collection<Field> fields = getSimpleTypeFields(source.getClass());

		try {
			for (Field f : fields) {
				f.setAccessible(true);
				Object sValue = f.get(source);
				Object tValue = f.get(target);

				if (Objects.equals(sValue, tValue) == false) {
					diff.add(f);
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return diff;
	}

	public static Map<String,Field> getAnnotatedFieldMap(Class<?> cls, Class<? extends Annotation> annotation) {
		return getAllFields(cls).stream()
				.filter(f -> f.getAnnotation(annotation) != null)
				.collect(Collectors.toMap(Field::getName, Function.identity()));
	}

	/**
	 * 어노테이션이 붙은 필드 중
	 * null이 아닌 필드정보 가져오기
	 *
	 * @param source
	 * @param annotation
	 * @param isBaseEnumCode true / baseEnum 구현체인 경우 code 값으로 내려 줌 그 외에는 enum 객체로 내려 줌
	 * @return
	 */
	public static Map<String, Object> getAnnotatedFieldIsNotNull(Object source, Class<? extends Annotation> annotation, Boolean isBaseEnumCode) {
		Map<String, Object> fieldNameObjectMap = new HashMap<>();
		BeanWrapper sourceWrapper = PropertyAccessorFactory.forBeanPropertyAccess(source);

		for (Field field : getAllFields(source.getClass())) {
			if (field.getAnnotation(annotation) != null) {
				Object value = sourceWrapper.getPropertyValue(field.getName());

				if(value != null) {
					if(isBaseEnumCode && field.getType().isEnum() && value instanceof BaseEnum) {
						BaseEnum enums  = (BaseEnum)value;
						value = enums.getCode();
					}
					fieldNameObjectMap.put(field.getName(), value);
				}
			}
		}
		return fieldNameObjectMap;
	}
}
