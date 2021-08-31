package com.brian.api.common.util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * request 범위 내에서의 처리를 위한 util
 */
@Slf4j
@Getter
@Setter
@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ExecutionTimeLogger {

    @Autowired
    private CustomObjectMapper mapper;

    private String processName;
    private StopWatch stopWatch;

    /**
     * request 로깅
     */
    public void start(HttpServletRequest request, HandlerMethod handlerMethod) {
        String className = handlerMethod.getBean().getClass().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();
        processName = "["+hashCode() + "-" + className + "." + methodName + "]";

        stopWatch = new StopWatch(processName);
        stopWatch.start();

        log.info("\n##### request start {}  uri: {} " , processName, request.getRequestURI());

        /**
         * request parameter 로깅
         */
        Map<String, String[]> paramMap = request.getParameterMap();
        if (CollectionUtils.isEmpty(paramMap) == false) {
            String paramString = mapper.writeValueAsPrettyString(paramMap);
            StringBuilder sb = new StringBuilder();
            sb.append("\n===== request parameters "+processName+"\n");
            sb.append(paramString);
            sb.append("\n===== request parameters");
            log.info(sb.toString());
        }

        /**
         * body 로깅
         */
        try {
            ServletInputStream is = request.getInputStream();

            if (is.isFinished() == false) {
                String bodyString = mapper.writeInputStreamAsPrettyString(is);

                if (bodyString != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n===== request body\n");
                    sb.append(bodyString);
                    sb.append("\n===== request body");
                    log.info(sb.toString());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void finish() {
        stopWatch.stop();
        long runningTime = stopWatch.getTotalTimeMillis();

        log.info("\n##### request end [{}] take time: {} ", processName, runningTime);

        if (runningTime > 5*1000) {
            log.warn("##### long running ==========> {}", stopWatch.shortSummary());
        }
    }
}
