package com.my.api.config;

import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Collection;

/**
 * validation message + Validator 설정
 */
@Configuration
public class RequestValidationConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/constraints");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean hntValidatorFactoryBean(MessageSource messageSource) {
        return new RequestValidator(messageSource);
    }

    /**
     * 요청 객체의 필드 검증기
     *
     * <p>스프링에서 기본으로 등록되는 요청 필드 검증기(LocalValidatorFactoryBean)는
     * <br>요청값이 Collection 인 경우, 내부 객체가 아닌 Collection 자체를 검증한다.
     * <br>Collection 에 담긴 객체를 검증하도록 오버라이드함
     */
    public class RequestValidator extends LocalValidatorFactoryBean {

        public RequestValidator(MessageSource messageSource) {
            /**
             * MessageInterpolator 설정해야만 커스터마이징한 메시지가 사용된다.
             * {@link #messageSource()}
             */
            MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory(messageSource);
            setMessageInterpolator(interpolatorFactory.getObject());
        }

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
