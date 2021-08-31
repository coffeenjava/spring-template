package com.brian.api.common.filter;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * request 커스트마이징 wrapper
 *
 * 1. InputStream 재활용
 */
public class CustomRequestWrapper extends HttpServletRequestWrapper {

    /**
     * request 의 InputStream 의 내용을 담아놓을 변수
     */
    private byte[] bytes;
    private boolean bodyRead;

    public CustomRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (bodyRead == false) {
            try {
                bytes = StreamUtils.copyToByteArray(super.getInputStream());
                bodyRead = true;
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }

        return new ServletInputStream() {

            /**
             * InputStream 을 매번 새로 생성
             */
            private ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }
}
