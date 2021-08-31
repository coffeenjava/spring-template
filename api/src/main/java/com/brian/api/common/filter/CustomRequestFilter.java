package com.brian.api.common.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 매 요청마다 단 한번 실행되는 OncePerRequestFilter 를 구현한 필터
 * request 로깅에 사용되므로 이 필터는 필수
 */
public class CustomRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // request 를 커스터마이징 request 로 변경
        request = new CustomRequestWrapper(request);

        filterChain.doFilter(request, response);
    }
}
