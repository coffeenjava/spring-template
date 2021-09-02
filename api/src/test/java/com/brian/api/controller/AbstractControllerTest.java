package com.brian.api.controller;

import com.brian.api.common.util.ResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public abstract class AbstractControllerTest {

    private String url;
    private String testFilePath;

    protected AbstractControllerTest(String url, String testFilePath) {
        this.url = url;
        this.testFilePath = testFilePath;
    }

    @Autowired
    protected MockMvc mvc;

    protected void testWithJson(HttpMethod method, String testFile, ResultMatcher matcher)
            throws Exception {
        String body = ResourceUtil.readJson("classpath:"+testFilePath+testFile);
        ResultActions actions = mvc.perform(request(method, url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        actions
                .andExpect(matcher)
                .andDo(print());
    }
}
