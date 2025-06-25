package com.example.cherrydan.campaign.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.cherrydan.campaign.domain.CampaignType;

@Getter
@Builder
public class CampaignResponseDTO {
    private Long id;
    private String title;
    private String detailUrl;
    private String benefit;
    private LocalDate applyStart;
    private LocalDate applyEnd;
    private LocalDate reviewerAnnouncement;
    private LocalDate contentSubmissionStart;
    private LocalDate contentSubmissionEnd;
    private LocalDate resultAnnouncement;
    private Integer applicantCount;
    private Integer recruitCount;
    private String sourceSite;
    private String imageUrl;
    @JsonIgnore private Boolean youtube;
    @JsonIgnore private Boolean shorts;
    @JsonIgnore private Boolean insta;
    @JsonIgnore private Boolean reels;
    @JsonIgnore private Boolean blog;
    @JsonIgnore private Boolean clip;
    @JsonIgnore private Boolean tiktok;
    @JsonIgnore private Boolean etc;
    private CampaignType campaignType;
    private String address;
    private Float competitionRate;
    private Integer localCategory;
    private Integer productCategory;

    @JsonProperty("platforms")
    public List<String> getPlatforms() {
        List<String> platforms = new ArrayList<>();
        if (Boolean.TRUE.equals(youtube)) platforms.add("youtube");
        if (Boolean.TRUE.equals(shorts)) platforms.add("shorts");
        if (Boolean.TRUE.equals(insta)) platforms.add("insta");
        if (Boolean.TRUE.equals(reels)) platforms.add("reels");
        if (Boolean.TRUE.equals(blog)) platforms.add("blog");
        if (Boolean.TRUE.equals(clip)) platforms.add("clip");
        if (Boolean.TRUE.equals(tiktok)) platforms.add("tiktok");
        if (Boolean.TRUE.equals(etc)) platforms.add("etc");
        return platforms;
    }
} 