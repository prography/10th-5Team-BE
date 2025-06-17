package com.example.cherrydan.version.repository;

import com.example.cherrydan.version.domain.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {

    @Query("SELECT av FROM AppVersion av ORDER BY av.createdAt DESC LIMIT 1")
    Optional<AppVersion> findLatestVersion();
}
