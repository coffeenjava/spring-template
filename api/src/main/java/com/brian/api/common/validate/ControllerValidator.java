package com.brian.api.common.validate;


import com.brian.api.common.annotation.ControllerValidation;
import com.brian.api.common.interceptor.ControllerInterceptor;

/**
 * 컨트롤러 진입 Validator 공통 인터페이스
 *
 *
 * @see ControllerValidation
 * @see ControllerInterceptor
 */
public interface ControllerValidator {

    // 조건이 필요한 경우 사용
    default <T> boolean supports(T t) {
        return true;
    }

    void execute(Object[] args);
}
