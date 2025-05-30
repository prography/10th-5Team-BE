package com.example.cherrydan.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이 컨트롤러는 실제로 API를 제공하지 않고 OAuth 인증 흐름을 문서화하기 위한 용도입니다.
 * Swagger UI에서 OAuth 인증 과정을 프론트엔드 개발자에게 설명하는 용도로 사용됩니다.
 */
@RestController
@RequestMapping("/api/docs")
@Tag(name = "인증", description = "OAuth2 인증 흐름 문서화")
public class AuthDocumentationController {

    @Operation(
            summary = "OAuth2 인증 시작",
            description = "소셜 로그인(Google, Kakao, Naver)을 시작하기 위한 엔드포인트입니다.<br>" +
                    "프론트엔드에서는 사용자를 다음 URL로 리다이렉트해야 합니다:<br>" +
                    "<code>GET /api/oauth2/authorize/{provider}</code><br>" +
                    "여기서 {provider}는 'kakao', 'naver' 중 하나입니다.<br><br>" +
                    "이 URL은 사용자를 해당 소셜 로그인 페이지로 리다이렉트합니다."
    )
    @GetMapping("/api/oauth2/authorization")
    public void oauthAuthorize() {
        // 이 메서드는 실제로 호출되지 않고 문서화 목적으로만 사용됩니다.
    }

    @Operation(
            summary = "토큰 리프레시",
            description = "액세스 토큰이 만료되었을 때 리프레시 토큰을 사용하여 새 액세스 토큰을 발급받는 엔드포인트입니다.<br>" +
                    "<code>POST api/auth/refresh?refreshToken={refreshToken}</code>"
    )
    @GetMapping("/auth/refresh")
    public void refreshToken() {
        // 이 메서드는 실제로 호출되지 않고 문서화 목적으로만 사용됩니다.
    }

    @Operation(
            summary = "프론트엔드 OAuth 콜백 처리 예시",
            description = "프론트엔드에서 OAuth 콜백을 처리하는 방법 예시입니다 (React 코드):<br><pre>" +
                    "// AuthCallback.jsx\n" +
                    "import { useEffect } from 'react';\n" +
                    "import { useNavigate, useLocation } from 'react-router-dom';\n\n" +
                    "function AuthCallback() {\n" +
                    "  const location = useLocation();\n" +
                    "  const navigate = useNavigate();\n\n" +
                    "  useEffect(() => {\n" +
                    "    const queryParams = new URLSearchParams(location.search);\n" +
                    "    const token = queryParams.get('token');\n" +
                    "    const refreshToken = queryParams.get('refreshToken');\n\n" +
                    "    if (token && refreshToken) {\n" +
                    "      // 토큰 저장\n" +
                    "      localStorage.setItem('accessToken', token);\n" +
                    "      localStorage.setItem('refreshToken', refreshToken);\n" +
                    "      \n" +
                    "      // 로그인 상태 업데이트 (Redux나 Context API 사용)\n" +
                    "      \n" +
                    "      // 메인 페이지로 이동\n" +
                    "      navigate('/');\n" +
                    "    } else {\n" +
                    "      // 인증 실패 처리\n" +
                    "      navigate('/api/auth/login', { state: { error: 'Authentication failed' } });\n" +
                    "    }\n" +
                    "  }, [location, navigate]);\n\n" +
                    "  return <div>로그인 처리 중...</div>;\n" +
                    "}\n\n" +
                    "export default AuthCallback;</pre>"
    )
    @GetMapping("/frontend-example")
    public void frontendExample() {
        // 이 메서드는 실제로 호출되지 않고 문서화 목적으로만 사용됩니다.
    }
}
