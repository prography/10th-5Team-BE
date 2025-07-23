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

    @Query("SELECT n FROM Notice n WHERE n.isActive = true ORDER BY n.isHot DESC, n.publishedAt DESC")
    Page<Notice> findActiveNoticesOrderByHotAndPublishedAt(Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.isActive = true AND n.category = :category ORDER BY n.isHot DESC, n.publishedAt DESC")
    Page<Notice> findActiveNoticesByCategoryOrderByHotAndPublishedAt(@Param("category") NoticeCategory category, Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.isActive = true AND n.isHot = true ORDER BY n.publishedAt DESC")
    List<Notice> findActiveHotNotices();

    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    int incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Notice n SET n.empathyCount = n.empathyCount + 1 WHERE n.id = :id")
    int incrementEmpathyCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Notice n SET n.empathyCount = n.empathyCount - 1 WHERE n.id = :id AND n.empathyCount > 0")
    int decrementEmpathyCount(@Param("id") Long id);
}
