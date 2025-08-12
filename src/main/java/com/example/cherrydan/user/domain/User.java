package com.example.cherrydan.user.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import com.example.cherrydan.oauth.model.AuthProvider;
import com.example.cherrydan.utils.MaskingUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    private String nickname;
    
    private String email;
    
    @Column(name = "birth_year")
    private Integer birthYear;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;
    
    @Column(name = "social_id")
    private String socialId;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuthProvider provider;
    
    @Column(name = "last_login")
    private String lastLogin;

    @Enumerated(EnumType.STRING)
    private Role role;
    
    // 프로필 이미지 URL
    private String picture;


    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "push_settings_id")
    private UserPushSettings pushSettings;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_tos_id")
    private UserTos userTos;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserKeyword> keywords = new ArrayList<>();

    // 소프트 삭제를 위한 활성 상태 필드
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // OAuth 정보 업데이트 (기존 사용자)
    public void updateOAuth2Info(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }

    public String getMaskedEmail() {
        return MaskingUtil.maskEmail(this.email);
    }

    // 소프트 삭제
    public void softDelete() {
        this.isActive = false;
    }

    // 계정 복구
    public void restore() {
        this.isActive = true;
    }

    // 활성 상태 확인
    public boolean isDeleted() {
        return !this.isActive;
    }
}
