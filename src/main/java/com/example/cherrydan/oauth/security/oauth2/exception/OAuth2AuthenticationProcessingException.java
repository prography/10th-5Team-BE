package com.example.cherrydan.oauth.security.oauth2.exception;

import com.example.cherrydan.common.exception.ErrorMessage;
import org.springframework.security.core.AuthenticationException;

public class OAuth2AuthenticationProcessingException extends AuthenticationException {
    private final ErrorMessage errorMessage;

    public OAuth2AuthenticationProcessingException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }

    public OAuth2AuthenticationProcessingException(String message) {
        super(message);
        this.errorMessage = null;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
