package com.example.cherrydan.common.exception;

public class OAuthStateException extends BaseException {
    public OAuthStateException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
