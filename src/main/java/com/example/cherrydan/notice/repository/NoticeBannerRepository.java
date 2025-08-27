package com.example.cherrydan.notice.repository;

import com.example.cherrydan.notice.domain.NoticeBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoticeBannerRepository extends JpaRepository<NoticeBanner, Long> {
    List<NoticeBanner> findByIsActiveTrueOrderByPriorityAsc();
} 