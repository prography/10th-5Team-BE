package com.example.cherrydan.inquiry.repository;

import com.example.cherrydan.inquiry.domain.Inquiry;
import com.example.cherrydan.inquiry.domain.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    @Query("SELECT i FROM Inquiry i WHERE i.user.id = :userId AND i.user.isActive = true ORDER BY i.createdAt DESC")
    Page<Inquiry> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT i FROM Inquiry i WHERE i.user.id = :userId AND i.status = :status AND i.user.isActive = true ORDER BY i.createdAt DESC")
    Page<Inquiry> findByUserIdAndStatusOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("status") InquiryStatus status, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Inquiry i WHERE i.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
