package com.example.cherrydan.common.exception;

import com.example.cherrydan.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 커스텀 BaseException 처리
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        ErrorMessage errorMessage = ex.getErrorMessage();
        logger.error("BaseException: {}", errorMessage.getMessage(), ex);
        return ResponseEntity.status(errorMessage.getHttpStatus())
                .body(ApiResponse.error(errorMessage.getHttpStatus().value(), errorMessage.getMessage()));
    }
    
    /**
     * AuthException 처리
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException ex) {
        ErrorMessage errorMessage = ex.getErrorMessage();
        logger.error("AuthException: {}", errorMessage.getMessage());
        return ResponseEntity.status(errorMessage.getHttpStatus())
                .body(ApiResponse.error(errorMessage.getHttpStatus().value(), errorMessage.getMessage()));
    }
    
    /**
     * UserException 처리
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserException(UserException ex) {
        ErrorMessage errorMessage = ex.getErrorMessage();
        logger.error("UserException: {}", errorMessage.getMessage());
        return ResponseEntity.status(errorMessage.getHttpStatus())
                .body(ApiResponse.error(errorMessage.getHttpStatus().value(), errorMessage.getMessage()));
    }
    
    /**
     * OAuthException 처리
     */
    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleOAuthException(OAuthException ex) {
        ErrorMessage errorMessage = ex.getErrorMessage();
        logger.error("OAuthException: {}", errorMessage.getMessage());
        return ResponseEntity.status(errorMessage.getHttpStatus())
                .body(ApiResponse.error(errorMessage.getHttpStatus().value(), errorMessage.getMessage()));
    }
    
    /**
     * SnsException 처리
     */
    @ExceptionHandler(SnsException.class)
    public ResponseEntity<ApiResponse<Void>> handleSnsException(SnsException ex) {
        ErrorMessage errorMessage = ex.getErrorMessage();
        logger.error("SnsException: {}", errorMessage.getMessage());
        return ResponseEntity.status(errorMessage.getHttpStatus())
                .body(ApiResponse.error(errorMessage.getHttpStatus().value(), errorMessage.getMessage()));
    }
    
    /**
     * FCMException 처리
     */
    @ExceptionHandler(FCMException.class)
    public ResponseEntity<ApiResponse<Void>> handleFCMException(FCMException ex) {
        ErrorMessage errorMessage = ex.getErrorMessage();
        logger.error("FCMException: {}", errorMessage.getMessage());
        return ResponseEntity.status(errorMessage.getHttpStatus())
                .body(ApiResponse.error(errorMessage.getHttpStatus().value(), errorMessage.getMessage()));
    }

    /**
     * AppVersionException 처리
     */
    @ExceptionHandler(AppVersionException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppVersionException(AppVersionException ex) {
        ErrorMessage errorMessage = ex.getErrorMessage();
        logger.error("AppVersionException: {}", errorMessage.getMessage());
        return ResponseEntity.status(errorMessage.getHttpStatus())
                .body(ApiResponse.error(errorMessage.getHttpStatus().value(), errorMessage.getMessage()));
    }

    /**
     * NoticeException 처리
     */
    @ExceptionHandler(NoticeException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoticeException(NoticeException ex) {
        ErrorMessage errorMessage = ex.getErrorMessage();
        logger.error("NoticeException: {}", errorMessage.getMessage());
        return ResponseEntity.status(errorMessage.getHttpStatus())
                .body(ApiResponse.error(errorMessage.getHttpStatus().value(), errorMessage.getMessage()));
    }

    /**
     * InquiryException 처리
     */
    @ExceptionHandler(InquiryException.class)
    public ResponseEntity<ApiResponse<Void>> handleInquiryException(InquiryException ex) {
        ErrorMessage errorMessage = ex.getErrorMessage();
        logger.error("InquiryException: {}", errorMessage.getMessage());
        return ResponseEntity.status(errorMessage.getHttpStatus())
                .body(ApiResponse.error(errorMessage.getHttpStatus().value(), errorMessage.getMessage()));
    }

    /**
     * CampaignException 처리
     */
    @ExceptionHandler(CampaignException.class)
    public ResponseEntity<ApiResponse<Void>> handleCampaignException(CampaignException ex) {
        ErrorMessage errorMessage = ex.getErrorMessage();
        logger.error("CampaignException: {}", errorMessage.getMessage());
        return ResponseEntity.status(errorMessage.getHttpStatus())
                .body(ApiResponse.error(errorMessage.getHttpStatus().value(), errorMessage.getMessage()));
    }


    /**
     * 유효성 검사 실패 예외 처리
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(Exception ex) {
        logger.error("Validation Error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."));
    }
    
    /**
     * 메소드 인자 타입 불일치 및 파라미터 누락 예외 처리
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequestExceptions(Exception ex) {
        logger.error("Bad Request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "잘못된 요청 형식입니다."));
    }
    
    /**
     * 지원하지 않는 HTTP 메소드 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        logger.error("Method Not Supported: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), "지원하지 않는 HTTP 메소드입니다."));
    }
    
    /**
     * RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
    }
    
    /**
     * 리소스 없음 예외 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNoResourceFound(NoResourceFoundException ex) {
        return ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Not Found");
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        logger.error("Unexpected Error: ", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.serverError("예상치 못한 오류가 발생했습니다."));
    }

}
