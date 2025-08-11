package com.example.cherrydan.oauth.model;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import com.example.cherrydan.user.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    // User 참조 제거 - User 중심 단방향 관계로 변경
}
