package com.example.cherrydan.notice.service;

import com.example.cherrydan.notice.dto.NoticeBannerResponseDTO;
import java.util.List;

public interface NoticeBannerService {
    List<NoticeBannerResponseDTO> getActiveBanners();
} 