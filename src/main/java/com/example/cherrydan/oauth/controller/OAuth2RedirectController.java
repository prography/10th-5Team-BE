package com.example.cherrydan.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/login/oauth2")
public class OAuth2RedirectController {

    /**
     * OAuth2 로그인 성공 후 리다이렉트되는 URL
     * 토큰 파라미터를 받아 oauth-test.html 페이지로 리다이렉트합니다.
     */
    @GetMapping("/success")
    public RedirectView success(@RequestParam(required = false) String token) {
        log.info("OAuth2 로그인 성공: 리다이렉트 처리");
        return new RedirectView("/oauth-test.html?token=" + token);
    }

    /**
     * OAuth2 로그인 실패 후 리다이렉트되는 URL
     * 메시지 파라미터를 받아 oauth-test.html 페이지로 리다이렉트합니다.
     */
    @GetMapping("/failure")
    public RedirectView failure(@RequestParam(required = false) String message) {
        log.info("OAuth2 로그인 실패: 리다이렉트 처리");
        return new RedirectView("/oauth-test.html?error=" + message);
    }
}
