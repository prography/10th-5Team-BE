package com.example.cherrydan.campaign.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campaign_site")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignSite extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_name_kr", nullable = false, length = 100)
    private String siteNameKr;

    @Column(name = "site_name_en", nullable = false, length = 100, unique = true)
    private String siteNameEn;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
} 