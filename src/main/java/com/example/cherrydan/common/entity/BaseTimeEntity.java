package com.example.cherrydan.common.entity;

import com.example.cherrydan.common.audit.StringDateTimeProvider;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created", updatable = false)
    private String created;

    @LastModifiedDate
    @Column(name = "updated")
    private String updated;
    
    @PrePersist
    public void onPrePersist() {
        this.created = StringDateTimeProvider.now();
        this.updated = this.created;
    }
    
    @PreUpdate
    public void onPreUpdate() {
        this.updated = StringDateTimeProvider.now();
    }
}
