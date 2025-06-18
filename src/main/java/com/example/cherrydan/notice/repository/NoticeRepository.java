
package com.example.cherrydan.notice.repository;

import com.example.cherrydan.notice.domain.Notice;
import com.example.cherrydan.notice.domain.NoticeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("SELECT n FROM Notice n WHERE n.isActive = true ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<Notice> findActiveNoticesOrderByPinnedAndPublishedAt(Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.isActive = true AND n.category = :category ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<Notice> findActiveNoticesByCategoryOrderByPinnedAndPublishedAt(@Param("category") NoticeCategory category, Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.isActive = true AND n.isPinned = true ORDER BY n.publishedAt DESC")
    List<Notice> findActivePinnedNotices();

    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    int incrementViewCount(@Param("id") Long id);
}
