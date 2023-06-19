package com.my.api.common.interceptor;

import com.my.api.common.model.BaseRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Controller 로의 요청 객체 검증기
 *
 * <p>초기화 및 검증을 수행하는 메서드를 호출한다.
 *
 * @see BaseRequest#initAndValidate(Object)
 */
@Aspect
@Component
public class RequestValidationAspect {

    /**
     * Controller 클래스 && 인자가 BaseRequest 구현체이거나 HntBaseRequest 구현체의 Collection 인 경우 동작
     */
    @Before("bean(*Controller) && " +
            "(execution(* *(.., com.my.api.common.model.BaseRequest+, ..)) || " +
            "execution(* *(.., java.util.Collection<com.my.api.common.model.BaseRequest+>+, ..)))")
    public void before(final JoinPoint joinPoint) {
        for (final Object arg : joinPoint.getArgs()) {
            BaseRequest.initAndValidate(arg);
        }
    }
}
