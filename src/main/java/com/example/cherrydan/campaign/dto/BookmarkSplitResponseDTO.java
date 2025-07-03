package com.example.cherrydan.campaign.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import org.springframework.data.domain.Page;
import com.example.cherrydan.common.response.PageListResponseDTO;

@Getter
@Builder
public class BookmarkSplitResponseDTO {
    private PageListResponseDTO<BookmarkResponseDTO> open;
    private PageListResponseDTO<BookmarkResponseDTO> closed;
} 