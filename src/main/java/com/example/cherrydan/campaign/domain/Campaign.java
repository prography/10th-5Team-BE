package com.example.cherrydan.campaign.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 캠페인 엔티티
 */
@Entity
@Table(name = "campaigns")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Campaign extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "detail_url", unique = true)
    private String detailUrl;

    @Column(name = "sns_type")
    private Integer snsType;

    private String benefit;

    @Column(name = "apply_start")
    private LocalDate applyStart;

    @Column(name = "apply_end")
    private LocalDate applyEnd;

    @Column(name = "reviewer_announcement")
    private LocalDate reviewerAnnouncement;

    @Column(name = "content_submission_start")
    private LocalDate contentSubmissionStart;

    @Column(name = "content_submission_end")
    private LocalDate contentSubmissionEnd;

    @Column(name = "result_announcement")
    private LocalDate resultAnnouncement;

    @Column(name = "applicant_count")
    private Integer applicantCount;

    @Column(name = "recruit_count")
    private Integer recruitCount;

    @Column(name = "source_site")
    private String sourceSite;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "campaign_type")
    private Integer campaignType;

    // 지역 정보
    @Enumerated(EnumType.STRING)
    @Column(name = "main_region")
    private Region mainRegion;  // 행정구역 (서울, 부산, 경기 등)

    @Enumerated(EnumType.STRING)
    @Column(name = "detail_region")
    private Region detailRegion;  // 상세지역 (강남/논현, 노원/강북 등)

    // 지역 카테고리
    @Enumerated(EnumType.STRING)
    @Column(name = "region_category")
    private RegionCategory regionCategory;

    // 소셜미디어 플랫폼 정보
    @Embedded
    private SocialPlatforms socialPlatforms;

    /**
     * CampaignType enum으로 변환
     */
    public CampaignType getCampaignTypeEnum() {
        return CampaignType.fromCode(this.campaignType);
    }

    /**
     * 캠페인이 활성화되어 있는지 확인
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    /**
     * 현재 신청 가능한 캠페인인지 확인
     */
    public boolean isApplicable() {
        if (!isActive()) {
            return false;
        }
        LocalDate now = LocalDate.now();
        return applyStart != null && applyEnd != null &&
               !now.isBefore(applyStart) && !now.isAfter(applyEnd);
    }
}
