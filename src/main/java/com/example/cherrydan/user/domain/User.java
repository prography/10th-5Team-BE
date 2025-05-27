package com.example.capstone.user.domain;

import com.example.capstone.oauth.model.AuthProvider;
import com.example.capstone.oauth.model.RefreshToken;
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

    private String password;

    private String name;

    private String picture;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshToken refreshToken;

    public void updateOAuth2Info(AuthProvider provider, String providerId, String name, String picture) {
        this.provider = provider;
        this.providerId = providerId;
        this.name = name;
        this.picture = picture;
    }
}
