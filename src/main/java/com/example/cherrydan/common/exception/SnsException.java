package com.example.cherrydan.common.exception;

/**
 * SNS 관련 예외
 * SNS 연동, 인증, 플랫폼 관련 오류를 처리합니다.
 */
public class SnsException extends BaseException {
    
    public SnsException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
    
    public SnsException(ErrorMessage errorMessage, String detail) {
        super(errorMessage);
        // 상세 정보를 로그에 기록하거나 추가 처리 가능
    }
} 