package com.my.api.common.model;

import com.my.api.common.annotation.Description;
import com.my.api.common.util.LogKey;
import lombok.Getter;

/**
 * 공통 형식으로 내려주기 위한 응답 객체
 *
 * <p><p>ex)
 * <pre>
 * {@code
 * {
 *     logKey: "88b20381-8104-4911-9755-19164398a358",
 *     data: {
 *         age: 10,
 *         name: "고길동"
 *     }
 * }
 * }
 * </pre>
 */
@Getter
public final class ApiCommonResponse<T> {

    @Description("요청 추적용 로그키")
    private String logKey;

    @Description("응답 데이터")
    private T data;

    public ApiCommonResponse(T data) {
        this.logKey = LogKey.get();
        this.data = data;
    }
}
