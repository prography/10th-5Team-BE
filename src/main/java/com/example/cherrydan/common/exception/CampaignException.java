package com.example.cherrydan.common.exception;

/**
 * 캠페인 관련 커스텀 예외
 */
public class CampaignException extends BaseException {
    
    public CampaignException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
