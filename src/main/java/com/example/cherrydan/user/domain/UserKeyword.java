package com.example.cherrydan.user.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_keywords", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "keyword"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserKeyword extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 50, nullable = false)
    private String keyword;
} 