package com.my.api.config;

import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Collection;

/**
 * {@link javax.validation.Valid} 선언된 요청 객체 검증 오버라이드
 */
@Configuration
public class RequestValidationConfig {

    @Bean
    public LocalValidatorFactoryBean hntValidatorFactoryBean() {
         return new RequestValidator();
    }

    /**
     * 요청 객체의 필드 검증기
     *
     * <p>스프링에서 기본으로 등록되는 요청 필드 검증기(LocalValidatorFactoryBean)는
     * <br>요청값이 Collection 인 경우, 내부 객체가 아닌 Collection 자체를 검증한다.
     * <br>Collection 에 담긴 객체를 검증하도록 오버라이드함
     */
    public class RequestValidator extends LocalValidatorFactoryBean {

        @Override
        public void validate(final Object target, final Errors errors) {
            if (target instanceof Collection) {
                final Collection c = (Collection) target;

                for (Object e : c) {
                    this.validate(e, errors);
                }
            } else {
                super.validate(target, errors);
            }
        }
    }
}
