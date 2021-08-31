package com.brian.api.common.aop;

import com.brian.api.common.annotation.ControllerValidation;
import com.brian.api.common.validate.ControllerValidator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 컨트롤러 진입시 검증이 필요한 요청용
 *
 * @see ControllerValidation
 * @see ControllerValidator
 */
@RequiredArgsConstructor
@Aspect
@Component
public class ControllerValidationAspect {

    private final ApplicationContext applicationContext;

    @Before("@annotation(com.brian.api.common.annotation.ControllerValidation)")
    public void before(JoinPoint jp) {
        Object[] args = jp.getArgs();

        MethodSignature signature = (MethodSignature) jp.getSignature();
        ControllerValidation annotation = signature.getMethod().getAnnotation(ControllerValidation.class);

        for (Class cls : annotation.value()) {
            ControllerValidator validator = (ControllerValidator) applicationContext.getBean(cls);
            validator.execute(args);
        }

    }
}
