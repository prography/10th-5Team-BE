package com.example.capstone.common.response;

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
     * 성공 응답 - 커스텀 메시지
     */
    public static <T> ApiResponse<T> success(String message, T result) {
        return new ApiResponse<>(200, message, result);
    }
    
    /**
     * 잘못된 요청 응답
     */
    public static <T> ApiResponse<T> badRequest() {
        return new ApiResponse<>(400, "불가능한 요청입니다.", null);
    }
    
    /**
     * 잘못된 요청 응답 - 커스텀 메시지
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null);
    }
    
    /**
     * 서버 오류 응답
     */
    public static <T> ApiResponse<T> serverError() {
        return new ApiResponse<>(500, "서버 오류가 발생했습니다.", null);
    }
    
    /**
     * 서버 오류 응답 - 커스텀 메시지
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return new ApiResponse<>(500, message, null);
    }
    
    /**
     * 인증 오류 응답
     */
    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(401, "인증이 필요합니다.", null);
    }
    
    /**
     * 권한 오류 응답
     */
    public static <T> ApiResponse<T> forbidden() {
        return new ApiResponse<>(403, "접근 권한이 없습니다.", null);
    }
    
    /**
     * 리소스를 찾을 수 없음 응답
     */
    public static <T> ApiResponse<T> notFound() {
        return new ApiResponse<>(404, "요청한 리소스를 찾을 수 없습니다.", null);
    }
    
    /**
     * 커스텀 에러 응답
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    
    /**
     * 빈 리스트 응답 생성 유틸리티 메소드
     */
    public static <T> ApiResponse<List<T>> emptyList() {
        return success(Collections.emptyList());
    }
}
