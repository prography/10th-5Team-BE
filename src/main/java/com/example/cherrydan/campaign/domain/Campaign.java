package com.example.cherrydan.campaign.domain;


import com.example.cherrydan.campaign.domain.CampaignType;
import com.example.cherrydan.common.entity.BaseTimeEntity;
import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "campaigns")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "detail_url", nullable = false, length = 255, unique = true)
    private String detailUrl;

    @Column(name = "benefit", length = 255)
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

    @Column(name = "source_site", length = 100)
    private String sourceSite;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("false")
    private Boolean isActive;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "youtube")
    @ColumnDefault("0")
    private Boolean youtube;

    @Column(name = "shorts")
    @ColumnDefault("0")
    private Boolean shorts;

    @Column(name = "insta")
    @ColumnDefault("0")
    private Boolean insta;

    @Column(name = "reels")
    @ColumnDefault("0")
    private Boolean reels;

    @Column(name = "blog")
    @ColumnDefault("0")
    private Boolean blog;

    @Column(name = "clip")
    @ColumnDefault("0")
    private Boolean clip;

    @Column(name = "tiktok")
    @ColumnDefault("0")
    private Boolean tiktok;

    @Column(name = "etc")
    @ColumnDefault("0")
    private Boolean etc;

    @Convert(converter = CampaignTypeConverter.class)
    @Column(name = "campaign_type")
    private CampaignType campaignType;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "competition_rate")
    private Float competitionRate;

    @Column(name = "local_category")
    private Integer localCategory;

    @Column(name = "product_category")
    private Integer productCategory;

    @Column(name = "region_group")
    private Integer regionGroup;

    @Column(name = "region_detail")
    private Integer regionDetail;

    @Column(name = "has_error")
    private Boolean hasError;
}