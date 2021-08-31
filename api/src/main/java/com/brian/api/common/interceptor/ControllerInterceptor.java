package com.brian.api.common.interceptor;

import com.brian.api.common.util.ExecutionTimeLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller 인터셉터
 * API 성능 로그
 * todo ModelAttribute 까지 검증 가능하도록 Request 검증도 추가해보자.
 */
@Slf4j
//@Profile({"local","dev"})
@RequiredArgsConstructor
@Component
public class ControllerInterceptor implements HandlerInterceptor {

    private final ExecutionTimeLogger timeLogger;

    /**
     * Controller 진입 전 실행
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Class<?> handlerMethodType = handlerMethod.getBeanType();

            // 로그 시작
            // todo 분기문 왜 썼더라..
            if (BasicErrorController.class.isAssignableFrom(handlerMethodType)) {
                timeLogger.start(request, handlerMethod);
            } else if (ErrorController.class.isAssignableFrom(handlerMethodType) == false) {
                timeLogger.start(request, handlerMethod);
            }


        } catch (ClassCastException e) {}

        return true;
    }

    /**
     * Controller 에서 리턴 전 실행
     * 내부에서 에러 발생할 경우 실행되지 않는다. 에러핸들링으로 이동.
     * (에러 핸들링 하지 않을 경우 스프링이 내부 에러(Nested Exception) 발생시키며 해당 에러 처리 서비스를 통해 preHandler 로 진입하게 됨)
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        try {
            Object bean = ((HandlerMethod)handler).getBean();
            if (BasicErrorController.class.isAssignableFrom(bean.getClass())) {
                log.error("BasicErrorController");
                return;
            } else if (ErrorController.class.isAssignableFrom(bean.getClass())) {
                // Nested Exception 인 경우 이벤트 clear 후 리턴
                // 캐치되지 않은 에러는 ExceptionHandleAdvice 에 추가하여 마무리되도록 하고 이곳으로 진입하지 않도록 하자.
                log.error("에러 처리 추가 필요합니다. bean: {}", bean.getClass().getName());
                return;
            }
        } catch (ClassCastException e) {}
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
    }
}
