package com.example.cherrydan.common.util;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
public class JwtSecretKeyProvider {

    private static final int MINIMUM_KEY_LENGTH = 32;

    private JwtSecretKeyProvider() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다");
    }

    public static SecretKey createSecretKey(String secret) {
        validateSecretKey(secret);
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static void validateSecretKey(String secret) {
        if (secret == null) {
            log.error("JWT secret이 null입니다");
            throw new IllegalArgumentException("JWT secret은 필수입니다");
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MINIMUM_KEY_LENGTH) {
            log.error("JWT secret 길이가 부족합니다. 현재: {}바이트, 최소: {}바이트",
                    keyBytes.length, MINIMUM_KEY_LENGTH);
            throw new IllegalArgumentException(
                    String.format("JWT secret은 최소 %d바이트 이상이어야 합니다 (현재: %d바이트)",
                            MINIMUM_KEY_LENGTH, keyBytes.length)
            );
        }

        log.debug("JWT secret 검증 완료: {}바이트", keyBytes.length);
    }
}
