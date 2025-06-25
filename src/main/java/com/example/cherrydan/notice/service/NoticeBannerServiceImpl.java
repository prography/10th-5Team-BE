package com.example.cherrydan.notice.service;

import com.example.cherrydan.notice.domain.NoticeBanner;
import com.example.cherrydan.notice.dto.NoticeBannerResponseDTO;
import com.example.cherrydan.notice.repository.NoticeBannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeBannerServiceImpl implements NoticeBannerService {
    private final NoticeBannerRepository noticeBannerRepository;

    @Override
    public List<NoticeBannerResponseDTO> getActiveBanners() {
        List<NoticeBanner> banners = noticeBannerRepository.findByIsActiveTrue();
        return banners.stream().map(banner -> NoticeBannerResponseDTO.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .bannerType(banner.getBannerType())
                .linkType(banner.getLinkType())
                .targetId(banner.getTargetId())
                .targetUrl(banner.getTargetUrl())
                .build())
            .collect(Collectors.toList());
    }
} 