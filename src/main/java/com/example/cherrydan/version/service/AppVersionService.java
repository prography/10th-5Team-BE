package com.example.cherrydan.version.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.AppVersionException;
import com.example.cherrydan.version.domain.AppVersion;
import com.example.cherrydan.version.dto.AppVersionResponseDTO;
import com.example.cherrydan.version.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppVersionService {

    private final AppVersionRepository appVersionRepository;

    /**
     * 최신 앱 버전 정보 조회
     */
    public AppVersionResponseDTO getLatestVersion() {
        AppVersion latestVersion = appVersionRepository.findLatestVersion()
                .orElseThrow(() -> new AppVersionException(ErrorMessage.APP_VERSION_NOT_FOUND));

        return AppVersionResponseDTO.from(latestVersion);
    }

    public AppVersionResponseDTO getVersionById(Long id) {
        AppVersion version = appVersionRepository.findById(id)
                .orElseThrow(() -> new AppVersionException(ErrorMessage.APP_VERSION_NOT_FOUND));

        return AppVersionResponseDTO.from(version);
    }
}
