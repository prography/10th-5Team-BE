package com.example.cherrydan.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T result;
    
    /**
     * 성공 응답 - 데이터와 함께
     */
    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(200, "API 요청이 성공했습니다.", result);
    }
    
    /**
     * 성공 응답 - 데이터 없음
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "API 요청이 성공했습니다.", null);
    }

    /**
     * 성공 응답 - 데이터 없고 메시지만 있음
     */
    public static ApiResponse<EmptyResponse> success(String message) {
        return new ApiResponse<>(200, message, new EmptyResponse());
    }
    
    /**
     * 성공 응답 - 커스텀 메시지
     */
    public static <T> ApiResponse<T> success(String message, T result) {
        return new ApiResponse<>(200, message, result);
    }
    /**
     * 서버 오류 응답 - 커스텀 메시지
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return new ApiResponse<>(500, message, null);
    }

    /**
     * 공통 오류 응답
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null);
    }
    
    /**
     * 인증 오류 응답
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
