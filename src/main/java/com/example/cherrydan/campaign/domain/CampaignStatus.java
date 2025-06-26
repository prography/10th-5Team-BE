package com.example.cherrydan.campaign.domain;

import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campaign_status")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatus extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Enumerated(EnumType.ORDINAL)
    private CampaignStatusType status;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
} 