package com.example.cherrydan.user.service;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.UserException;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.UserTos;
import com.example.cherrydan.user.dto.UserTosRequestDTO;
import com.example.cherrydan.user.dto.UserTosResponseDTO;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserTosService {
    private final UserRepository userRepository;

    /**
     * 사용자 이용약관 동의 설정 조회
     */
    public UserTosResponseDTO getUserTos(Long userId) {
        log.info("이용약관 동의 설정 조회 시작 - userId: {}", userId);
        
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        UserTos userTos = getOrCreateUserTos(user);

        log.info("이용약관 동의 설정 조회 완료 - userId: {}", userId);
        return UserTosResponseDTO.from(userTos);
    }

    /**
     * 사용자 이용약관 동의 설정 업데이트
     */
    public UserTosResponseDTO updateUserTos(Long userId, UserTosRequestDTO request) {
        log.info("이용약관 동의 설정 업데이트 시작 - userId: {}, request: {}", userId, request);
        
        if (request == null) {
            throw new UserException(ErrorMessage.INVALID_REQUEST);
        }
        
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        UserTos userTos = getOrCreateUserTos(user);

        try {
            // 필수 동의사항 업데이트
            userTos.updateEssentialConsent(
                    request.getIsAgreedServiceUsage(),
                    request.getIsAgreedPrivateInfo(),
                    request.getIsAgreed3rdParty()
            );

            // 위치정보 동의 업데이트
            userTos.updateLocationConsent(request.getIsAgreedLocationInfo());

            // 마케팅 정보 동의 업데이트
            userTos.updateAdsConsent(request.getIsAgreedAds());

            log.info("이용약관 동의 설정 업데이트 완료 - userId: {}", userId);
            return UserTosResponseDTO.from(userTos);
        } catch (Exception e) {
            log.error("이용약관 동의 설정 업데이트 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new UserException(ErrorMessage.INVALID_REQUEST);
        }
    }

    /**
     * UserTos가 없으면 생성해서 반환하는 유틸 메서드
     */
    private UserTos getOrCreateUserTos(User user) {
        UserTos userTos = user.getUserTos();
        if (userTos == null) {
            log.info("기본 이용약관 동의 설정 생성 - userId: {}", user.getId());
            userTos = createDefaultUserTos(user);
        }
        return userTos;
    }

    /**
     * 기본 이용약관 동의 설정 생성
     */
    public UserTos createDefaultUserTos(User user) {
        log.info("기본 이용약관 동의 설정 생성 - userId: {}", user.getId());
        
        try {
            UserTos defaultUserTos = UserTos.builder()
                    .isAgreedServiceUsage(true)
                    .isAgreedPrivateInfo(true)
                    .isAgreed3rdParty(false)
                    .isAgreedLocationInfo(false)
                    .isAgreedAds(false)
                    .build();

            user.setUserTos(defaultUserTos);

            log.info("기본 이용약관 동의 설정 생성 완료 - userId: {}", user.getId());
            return defaultUserTos;
        } catch (Exception e) {
            log.error("기본 이용약관 동의 설정 생성 실패 - userId: {}, error: {}", user.getId(), e.getMessage());
            throw new UserException(ErrorMessage.INVALID_REQUEST);
        }
    }
} 