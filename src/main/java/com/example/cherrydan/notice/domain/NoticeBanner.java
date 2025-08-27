package com.example.cherrydan.notice.domain;

import lombok.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.example.cherrydan.common.entity.BaseTimeEntity;

@Entity
@Table(name = "notice_banner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class NoticeBanner extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subTitle;
    private String backgroundColor;
    private int priority;
    private String bannerType; // NOTICE, EVENT, AD 등
    private String linkType;   // INTERNAL, EXTERNAL
    private Long targetId;     // 내부 이동용(상세 id)
    private String targetUrl;  // 외부 이동용(광고 url)
    private Boolean isActive;
} 