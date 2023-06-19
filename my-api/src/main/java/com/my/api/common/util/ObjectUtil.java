package com.my.api.common.util;

import com.my.api.common.annotation.CopyAlias;
import com.my.api.common.annotation.Description;
import com.my.api.common.annotation.NoCopy;
import com.my.api.common.model.BaseEnum;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
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
	 * 객체의 필드 복사
	 * <li>getter/setter 가 있을 경우 동작
	 * <li>NoCopy 어노테이션 선언된 필드 제외
	 * <li>Map, Entity, Entity Collection(AbstractPersistentCollection) 이면 제외
	 *     <p>Entity 내부의 Entity 는 복사하지 않도록 함
	 * <li>CopyAlias 어노테이션 선언된 필드에도 copy
	 * <p>
	 * @param source	원본
	 * @param target	대상
	 * @param includeNull	null 필드 포함 여부.
	 *                      true 인 경우 null 필드도 copy
	 */
	public static <T> T copyProperties(final Object source, final T target,
									   final boolean includeNull) throws BeansException {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");

		/**
		 * 소스필드와 타겟필드을 담아둔다
		 */
		final Map<String, Field> sourceFieldsMap = ObjectUtil.getAllFieldsMap(source.getClass());
		final Map<String, Field> targetFieldMap = ObjectUtil.getAllFieldsMap(target.getClass());

		// 소스필드와 타겟필드를 담아둘 Map
		final MultiValueMap<Field, Field> copyFieldMap = new LinkedMultiValueMap<>();

		for (final Map.Entry<String, Field> e : sourceFieldsMap.entrySet()) {
			final Field sourceField = e.getValue();

			// copy 제외할 필드 처리
			final NoCopy noCopy = sourceField.getAnnotation(NoCopy.class);

			boolean isCopy = true;
			if (noCopy != null) {
				for (final Class noCopyTarget : noCopy.value()) {
					if (noCopyTarget == Void.class || noCopyTarget == target.getClass()) {
						isCopy = false;
						break;
					}
				}
			}

			if (isCopy) {
				final Field targetField = targetFieldMap.get(e.getKey());

				if (targetField != null) {
					copyFieldMap.add(sourceField, targetField);
				}
			}

			// alias 필드 처리
			final CopyAlias copyAlias = sourceField.getAnnotation(CopyAlias.class);
			if (copyAlias != null) {
				for (final String alias : copyAlias.value()) {
					final Field targetField = targetFieldMap.get(alias);

					if (targetField != null) {
						copyFieldMap.add(sourceField, targetField);
					}
				}
			}
		}

		try {
			/**
			 * 필드 copy
			 */
			final BeanWrapper sourceWrapper = PropertyAccessorFactory.forBeanPropertyAccess(source);
			final BeanWrapper targetWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);

			for (Map.Entry<Field, List<Field>> e : copyFieldMap.entrySet()) {
				final Field sourceField = e.getKey();
				Object value; // copy 할 값

				if (sourceWrapper.isReadableProperty(sourceField.getName())) {
					// getter 가 존재하면 getter 호출로 값 추출
					value = sourceWrapper.getPropertyValue(sourceField.getName());
				} else {
					// 필드에서 직접 값 추출
					sourceField.setAccessible(true);
					value = sourceField.get(source);
				}

				// 값이 null 이고 null 포함 copy 가 아니면 제외
				if (value == null && includeNull == false) {
					continue;
				}

				// Map, Entity 이면 제외
				// 연관관계 있는 경우 등의 이슈로 Entity 제외
				if (value != null && (value instanceof Map || isEntity(value))) {
					continue;
				}

				// Entity Collection 이면 제외
				// 내부 값에 접근하면 Lazy 엔티티에 대한 조회가 발생하므로 여기서 제외한다.
				if (value instanceof AbstractPersistentCollection) {
					continue;
				}

				final List<Field> targetFieldList = copyFieldMap.get(sourceField);

				for (final Field targetField : targetFieldList) {
					if (value != null) {
						if (value instanceof Collection) { // 컬랙션인 경우
							final Collection collectionValue = (Collection) value;

							if (collectionValue.isEmpty()) {
								continue;
							}

							final Class sourceType = collectionValue.iterator().next().getClass();
							final Class targetType = getGenericTypeFromCollection(target.getClass(), targetField.getName());

							// 소스 컬렉션 내의 엘리먼트 타입으로 타입 컬렉션에 들어갈 엘리먼트 인스턴스를 생성하므로
							// 내부 엘리먼트의 타입이 일치하지 않으면 제외
							// 타입이 달라도 copy 는 동작하지만 복사된 값을 꺼낼때 cast 에러 발생한다.
							//
							// 2022.03.01. 타입이 달라도 값 복사하도록 수정
							// 대상의 타입으로 컬렉션을 생성함으로써 필드명만 같으면 복사하도록 한다.
							// 대상이 인터페이스면 생성이 안되므로 소스 타입 컬렉션으로 생성
//                        if (sourceType != targetType) {
//                            continue;
//                        }

							final Collection newCollection;

							if (targetField.getType().isInterface()) { // 대상이 인터페이스이면
								newCollection = createInstance(collectionValue.getClass()); // 소스 타입으로 생성
							} else {
								newCollection = (Collection) createInstance(targetField.getType()); // 대상 타입으로 생성
							}

							final boolean isSimpleValue = BeanUtils.isSimpleValueType(sourceType);
							if (isSimpleValue) {
								newCollection.addAll(collectionValue);
							} else {
								// complex type 요소(DTO 등)이면 재귀호출
								collectionValue.forEach(c -> {
									final Object targetElement = createInstance(targetType);
									copyProperties(c, targetElement, includeNull);
									newCollection.add(targetElement);
								});
							}

							// copy 할 값을 새로 만든 컬렉션으로 변경
							value = newCollection;

						} else if (value.getClass().isArray()) {
							// deep copy 아님
							// simple type 배열이 아닌 경우까지는 고려하지 않음
							final int length = Array.getLength(value);
							final Object newArray = Array.newInstance(targetField.getType().getComponentType(), length);

							for (int i = 0; i < length; i++) {
								Array.set(newArray, i, Array.get(value, i));
							}

							// copy 할 값을 새로 만든 배열로 변경
							value = newArray;

						} else if (BeanUtils.isSimpleValueType(value.getClass()) == false) {
							final Object targetElement = createInstance(targetField.getType());
							copyProperties(value, targetElement, includeNull);
							value = targetElement;
						}
					}

					// copy
					if (targetWrapper.isWritableProperty(targetField.getName())) {
						// setter 가 존재하면 setter 호출로 값을 할당
						targetWrapper.setPropertyValue(targetField.getName(), value);
					} else {
						// 필드에 직접 값을 할당
						targetField.setAccessible(true);
						targetField.set(target, value);
					}
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return target;
	}

	public static boolean isEntity(Object o) {
		return isEntity(o.getClass());
	}

	public static boolean isEntity(Class cls) {
		return AnnotatedElementUtils.hasAnnotation(cls, Entity.class);
	}

	public static <T> T createInstance(final Class<T> cls) {
		try {
			final Constructor<T> ctor = cls.getDeclaredConstructor();
			ctor.setAccessible(true);
			return ctor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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
	 * IntelliJ의 Coverage 를 돌리면 ignoreFieldPattern 패턴에 해당하는 필드를 심어서 Refection이 오작동 한다.
	 * 필드를 추출할때 해당 패턴은 제외시킨다.
	 */
	private static Pattern ignoreFieldPattern = Pattern.compile("__\\$[a-zA-Z]+\\$__");

	/**
	 * 클래스의 모든 필드 가져오기
	 *
	 * 상속받은 필드 포함
	 * 중복 선언된 필드의 경우 하위 우선
	 */
	public static Map<String, Field> getAllFieldsMap(final Class<?> type) {
		final Map<String, Field> fieldMap = new HashMap<>();


		ReflectionUtils.doWithFields(type, field -> {
					if (fieldMap.containsKey(field.getName()) == false) {
						fieldMap.put(field.getName(), field);
					}
				}, field -> !ignoreFieldPattern.matcher(field.getName()).matches()
		);

		return fieldMap;
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
