package com.example.cherrydan.user.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import com.example.cherrydan.oauth.model.AuthProvider;
import com.example.cherrydan.oauth.model.RefreshToken;
import com.example.cherrydan.utils.MaskingUtil;
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

    public String getMaskedEmail() {
        return MaskingUtil.maskEmail(this.email);
    }
}
