package com.example.cherrydan.user.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import com.example.cherrydan.oauth.model.AuthProvider;
import com.example.cherrydan.oauth.model.RefreshToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", schema = "oddong")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    private String nickname;
    
    private String email;
    
    private String mdn;
    
    @Column(name = "social_id")
    private String socialId;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuthProvider provider;
    
    private String uuid;
    
    @Column(name = "fcm_token")
    private String fcmToken;
    
    @Column(name = "app_version")
    private String appVersion;
    
    @Column(name = "os_version")
    private String osVersion;
    
    @Column(name = "device_model")
    private String deviceModel;
    
    @Column(name = "last_login")
    private String lastLogin;
    
    // 프로필 이미지 URL
    private String picture;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshToken refreshToken;

    // OAuth 정보 업데이트 (기존 사용자)
    public void updateOAuth2Info(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }

    // 새로운 OAuth 사용자 생성
    public static User createOAuthUser(String email, String name, String picture, String socialId, 
                                     AuthProvider provider) {
        return User.builder()
                .email(email)
                .name(name)
                .picture(picture)
                .socialId(socialId)
                .provider(provider)
                .build();
    }
}
