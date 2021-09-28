package com.brian.api.common.aop;

import com.brian.api.common.model.BaseRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Set;

/**
 * request argument validation 처리
 *
 * 어노테이션 @RequestBody 선언된 argument가(컬렉션인 경우 내부 객체) BaseRequestDto 구현체인 경우
 * validate()를 호출한다.
 *
 * Collection argument 는 내부 객체에 대한 검증(javax.validation.Validator)을 직접 수행
 * (주의! Collection 자체에 대한 @Valid 선언과 무관하게 수행하므로 @Valid 선언을 하면 중복 검증이므로 선언하지 않도록 하자.)
 *
 * todo ControllerInterceptor 로 기능을 옮길 수 있는지 확인해보자.
 */
@RequiredArgsConstructor
@Aspect
@Component
public class RequestValidationAspect {

	private final Validator validator;

	@Before("execution(* *(.., @org.springframework.web.bind.annotation.RequestBody (*), ..))")
	@Order
	public void before(JoinPoint joinPoint) {
		for (Object arg : joinPoint.getArgs()) {
			if (arg instanceof Collection) {
				/**
				 * 컨트롤러 메서드의 argument 에 @Valid 가 있는 경우 SpringValidatorAdapter 가 동작하여 검증을 진행한다.(기본)
				 * 그러나 컬렉션 argument 의 경우 내부에 담긴 객체에 대한 Validation 을 진행하지 않는다.
				 *
				 * 컬렉션은 컨트롤러에 @Validated 가 있을 경우에만 MethodValidationInterceptor 를 통해 실행되며
				 * MethodValidationInterceptor 는 우리가 등록한 AOP 를 실행하는 Interceptor(MethodBeforeAdviceInterceptor) 보다
				 * 나중에 동작한다.
				 *
				 * 결과적으로 각 필드 검증 전에 validate() 메서드가 실행되어 검증 순서가 꼬이는 문제가 발생.
				 * 이를 막기 위해 @Validated 사용하지 않고 여기서 직접 컬렉션 내부 객체에 대한 검증을 수행하도록 하였다.
				 *
				 * BeanValidationPostProcessor.doValidate 소스 참고
				 */
				Collection c = (Collection) arg;

				for (Object e : c) {
					Set<ConstraintViolation<Object>> result = this.validator.validate(e);

					if (!result.isEmpty()) {
						throw new ConstraintViolationException(result);
					}
				}

				// 위의 방식을 통한 통작에 이슈가 있을 경우 아래 코드를 활용
				// MethodValidationInterceptor.invoke 소스 참고
//				ExecutableValidator execVal = this.validator.forExecutables();
//				Method methodToValidate = ((MethodSignature)joinPoint.getSignature()).getMethod();
//				Set<ConstraintViolation<Object>> result;
//				result = execVal.validateParameters(
//						joinPoint.getTarget(), methodToValidate, new Object[]{arg}, new Class<?>[0]);
//
//				if (!result.isEmpty()) {
//					throw new ConstraintViolationException(result);
//				}
			}

			// BaseRequest 구현 객체의 valdiate() 호출하여 검증
			BaseRequest.validate(arg);
		}
	}
}
