package com.example.cherrydan.version.domain;

import com.example.cherrydan.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_version")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version", nullable = false, length = 30)
    private String version;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
