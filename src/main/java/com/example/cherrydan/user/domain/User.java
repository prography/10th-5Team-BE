package com.example.cherrydan.user.domain;

import com.example.cherrydan.oauth.model.AuthProvider;
import com.example.cherrydan.oauth.model.RefreshToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshToken refreshToken;

    // OAuth 정보 업데이트 (기존 사용자)
    public void updateOAuth2Info(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }

    // 새로운 OAuth 사용자 생성
    public static User createOAuthUser(String email, String name, String picture, 
                                     AuthProvider provider, String providerId) {
        return User.builder()
                .email(email)
                .name(name)
                .picture(picture)
                .provider(provider)
                .providerId(providerId)
                .role(Role.ROLE_USER)
                .build();
    }
}
