package com.brian.api.controller;

import com.brian.api.common.aop.RequestValidationAspect;
import com.brian.api.config.CustomMessageSourceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = HelloController.class) // 웹 요청/응답 관련(Controller etc) 테스트. 지정하지 않으면 모든 Controller 가 빈으로 등록된다.
@Import({ // 적용할 config 나 aop
        AopAutoConfiguration.class, RequestValidationAspect.class,
        CustomMessageSourceConfig.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 하나의 인스턴스로 모든 테스트 메서드 수행
public class HelloControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void hello() throws Exception {
        mvc.perform(get("/hello").param("name","brian"))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
