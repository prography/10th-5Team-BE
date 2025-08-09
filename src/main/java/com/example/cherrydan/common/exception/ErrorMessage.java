package com.example.cherrydan.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ErrorMessage {
    // 유저 관련 에러
    USER_NOT_FOUND(NOT_FOUND, "존재하지 않는 회원입니다."),
    USER_EMAIL_ALREADY_EXISTS(BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    USER_USERNAME_ALREADY_EXISTS(BAD_REQUEST, "이미 사용 중인 사용자 이름입니다."),
    USER_INVALID_CREDENTIALS(UNAUTHORIZED, "잘못된 이메일 또는 비밀번호입니다."),

    // 캠페인 관련 에러
    CAMPAIGN_REGION_GROUP_NOT_FOUND(NOT_FOUND, "존재하지 않는 대분류 지역명입니다."),
    CAMPAIGN_REGION_DETAIL_NOT_FOUND(NOT_FOUND, "존재하지 않는 하위 지역명입니다."),
    CAMPAIGN_PRODUCT_CATEGORY_NOT_FOUND(NOT_FOUND, "존재하지 않는 제품 카테고리입니다."),
    CAMPAIGN_REPORTER_NOT_FOUND(NOT_FOUND, "존재하지 않는 기자단입니다."),
    CAMPAIGN_SNS_NOT_FOUND(NOT_FOUND, "존재하지 않는 SNS 플랫폼입니다."),
    CAMPAIGN_EXPERIENCE_PLATFORM_NOT_FOUND(NOT_FOUND, "존재하지 않는 체험단 플랫폼입니다."),
    
    // OAuth 관련 에러
    OAUTH_DUPLICATE_EMAIL(BAD_REQUEST, "이미 다른 소셜 계정으로 가입된 이메일입니다."),
    OAUTH_USER_INFO_NOT_FOUND(BAD_REQUEST, "OAuth 사용자 정보를 가져올 수 없습니다."),
    OAUTH_PROVIDER_NOT_SUPPORTED(BAD_REQUEST, "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_AUTHENTICATION_FAILED(UNAUTHORIZED, "OAuth 인증에 실패했습니다."),
    
    // Apple 관련 에러
    APPLE_IDENTITY_TOKEN_INVALID(UNAUTHORIZED, "Apple Identity Token이 유효하지 않습니다."),
    APPLE_IDENTITY_TOKEN_EXPIRED(UNAUTHORIZED, "Apple Identity Token이 만료되었습니다."),
    APPLE_PUBLIC_KEY_NOT_FOUND(UNAUTHORIZED, "일치하는 Apple 공개키를 찾을 수 없습니다."),
    APPLE_JWT_VERIFICATION_FAILED(UNAUTHORIZED, "Apple JWT 검증에 실패했습니다."),
    APPLE_USER_INFO_MISSING(BAD_REQUEST, "Apple 사용자 정보가 누락되었습니다."),
    
    // 인증 관련 에러
    AUTH_INVALID_TOKEN(UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(UNAUTHORIZED, "만료된 토큰입니다."),
    AUTH_INVALID_REFRESH_TOKEN(UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    AUTH_EXPIRED_REFRESH_TOKEN(UNAUTHORIZED, "만료된 리프레시 토큰입니다."),
    AUTH_REFRESH_TOKEN_NOT_FOUND(UNAUTHORIZED, "리프레시 토큰이 없습니다."),
    AUTH_TOKEN_NOT_FOUND(UNAUTHORIZED, "인증 토큰이 없습니다."),
    AUTH_UNAUTHORIZED(UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    AUTH_ACCESS_DENIED(FORBIDDEN, "접근 권한이 없습니다."),
    AUTH_HEADER_MISSING(UNAUTHORIZED, "Authorization 헤더가 필요합니다."),
    AUTH_INVALID_TOKEN_TYPE(UNAUTHORIZED, "올바르지 않은 토큰 타입입니다."),

    // JWT 관련 에러
    JWT_INVALID_SIGNATURE(UNAUTHORIZED, "JWT 서명이 유효하지 않습니다."),
    JWT_MALFORMED(UNAUTHORIZED, "잘못된 형식의 JWT입니다."),
    JWT_UNSUPPORTED(UNAUTHORIZED, "지원하지 않는 JWT입니다."),
    JWT_CLAIMS_EMPTY(UNAUTHORIZED, "JWT claims가 비어있습니다."),

    // 리프레쉬 토큰 에러
    REFRESH_TOKEN_DELETE_ERROR(BAD_REQUEST, "토큰 삭제에 실패했습니다."),

    // FCM 관련 에러
    FCM_TOKEN_INVALID_REQUEST(BAD_REQUEST, "FCM 토큰 요청 데이터가 유효하지 않습니다."),
    FCM_TOKEN_NOT_FOUND(NOT_FOUND, "FCM 토큰을 찾을 수 없습니다."),
    FCM_TOKEN_REGISTRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 등록에 실패했습니다."),
    FCM_TOKEN_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 업데이트에 실패했습니다."),
    FCM_TOKEN_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 삭제에 실패했습니다."),
    FCM_DEVICE_TYPE_INVALID(BAD_REQUEST, "지원하지 않는 디바이스 타입입니다."),
    FCM_TOKEN_ACCESS_DENIED(FORBIDDEN, "FCM 토큰에 대한 접근 권한이 없습니다."),
    FCM_SERVICE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 서비스를 사용할 수 없습니다."),
    FCM_NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송에 실패했습니다."),
    FCM_NOTIFICATION_INVALID_REQUEST(BAD_REQUEST, "알림 요청 데이터가 유효하지 않습니다."),
    FCM_USER_NO_ACTIVE_TOKENS(NOT_FOUND, "사용자의 활성화된 FCM 토큰이 없습니다."),

    // 알림 관련 에러
    NOTIFICATION_FIREBASE_INIT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Firebase 초기화에 실패했습니다."),
    NOTIFICATION_SERVICE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "알림 서비스를 사용할 수 없습니다."),
    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송에 실패했습니다."),
    NOTIFICATION_USER_NO_TOKENS(NOT_FOUND, "사용자의 활성화된 토큰이 없습니다."),
    NOTIFICATION_TOPIC_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토픽 알림 전송에 실패했습니다."),
    NOTIFICATION_TOKEN_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 알림 전송에 실패했습니다."),
    NOTIFICATION_MULTIPLE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "다중 알림 전송에 실패했습니다."),

    // 앱 버전 관련 에러
    APP_VERSION_NOT_FOUND(NOT_FOUND, "앱 버전 정보를 찾을 수 없습니다."),

    // 공지사항 관련 에러
    NOTICE_NOT_FOUND(NOT_FOUND, "공지사항을 찾을 수 없습니다."),
    NOTICE_INACTIVE(BAD_REQUEST, "비활성화된 공지사항입니다."),
    NOTICE_CATEGORY_INVALID(BAD_REQUEST, "잘못된 공지사항 카테고리입니다."),
    INVALID_ACTION(BAD_REQUEST, "잘못된 액션입니다. 'up' 또는 'down'을 입력해주세요."),

    // SNS 연동 관련 에러
    SNS_PLATFORM_NOT_SUPPORTED(BAD_REQUEST, "지원하지 않는 SNS 플랫폼입니다."),
    SNS_CONNECTION_NOT_FOUND(NOT_FOUND, "SNS 연동 정보를 찾을 수 없습니다."),
    SNS_TOKEN_ACQUISITION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SNS 액세스 토큰 획득에 실패했습니다."),
    SNS_USER_INFO_ACQUISITION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SNS 사용자 정보 획득에 실패했습니다."),
    SNS_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SNS 연동에 실패했습니다."),
    SNS_TOKEN_REFRESH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SNS 토큰 갱신에 실패했습니다."),

    // 문의 관련 에러
    INQUIRY_NOT_FOUND(NOT_FOUND, "문의 정보를 찾을 수 없습니다."),
    INQUIRY_ACCESS_DENIED(FORBIDDEN, "본인의 문의가 아닙니다."),
    INQUIRY_CATEGORY_INVALID(BAD_REQUEST, "잘못된 문의 카테고리입니다."),
    INQUIRY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "문의 등록에 실패했습니다."),
    INQUIRY_REPLY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "문의 답변 등록에 실패했습니다."),

    // 푸시 알림 설정 관련 에러
    PUSH_SETTINGS_NOT_FOUND(NOT_FOUND, "푸시 알림 설정을 찾을 수 없습니다."),
    PUSH_SETTINGS_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "푸시 알림 설정 생성에 실패했습니다."),
    PUSH_SETTINGS_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "푸시 알림 설정 업데이트에 실패했습니다."),
    PUSH_SETTINGS_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "푸시 알림 설정 삭제에 실패했습니다."),
    PUSH_CATEGORY_INVALID(BAD_REQUEST, "잘못된 푸시 알림 카테고리입니다."),
    PUSH_SETTINGS_ACCESS_DENIED(FORBIDDEN, "푸시 알림 설정에 대한 접근 권한이 없습니다."),
    PUSH_SETTINGS_INVALID_REQUEST(BAD_REQUEST, "잘못된 푸시 알림 설정 요청입니다."),

    // 공통 에러
    INVALID_REQUEST(BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_PARAMETER(BAD_REQUEST, "잘못된 파라미터입니다."),
    RESOURCE_NOT_FOUND(NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류가 발생했습니다."),

    // 네이버 블로그 관련 에러
    NAVER_BLOG_NOT_FOUND(NOT_FOUND, "네이버 블로그를 찾을 수 없습니다."),
    NAVER_BLOG_INVALID_URL(BAD_REQUEST, "올바른 네이버 블로그 주소가 아닙니다."),
    NAVER_BLOG_INVALID_DESCRIPTION(BAD_REQUEST, "네이버 블로그 description에 인증코드가 없습니다."),
    NAVER_BLOG_INVALID_ID(BAD_REQUEST, "네이버 블로그 ID에 유효하지 않은 문자가 포함되어 있습니다."),

    // 사용자 키워드 관련 에러
    USER_KEYWORD_ALREADY_EXISTS(BAD_REQUEST, "이미 등록된 키워드입니다."),
    USER_KEYWORD_NOT_FOUND(NOT_FOUND, "등록된 키워드를 찾을 수 없습니다."),
    USER_KEYWORD_LIMIT_EXCEEDED(BAD_REQUEST, "키워드 등록 개수가 한도를 초과했습니다."),
    USER_KEYWORD_ACCESS_DENIED(FORBIDDEN, "본인의 키워드/알림만 처리할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
