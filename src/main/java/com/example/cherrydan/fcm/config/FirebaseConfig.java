package com.example.cherrydan.fcm.config;

import com.example.cherrydan.common.exception.ErrorMessage;
import com.example.cherrydan.common.exception.NotificationException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Configuration
 * Firebase Admin SDK 초기화 및 설정을 담당하는 클래스
 *
 */
//@Slf4j
//@Configuration
//public class FirebaseConfig {
//
//    @Value("${fcm.service-account-key-path:classpath:firebase-service-account.json}")
//    private String serviceAccountKeyPath;
//
//    /**
//     * Firebase App 초기화
//     * 애플리케이션 시작 시 Firebase Admin SDK를 초기화합니다.
//     */
//    @PostConstruct
//    public void initializeFirebase() {
//        try {
//            // Firebase App이 이미 초기화되었는지 확인
//            if (FirebaseApp.getApps().isEmpty()) {
//                // Service Account Key 파일 로드
//                InputStream serviceAccount;
//
//                if (serviceAccountKeyPath.startsWith("classpath:")) {
//                    String path = serviceAccountKeyPath.substring("classpath:".length());
//                    serviceAccount = new ClassPathResource(path).getInputStream();
//                } else {
//                    serviceAccount = new ClassPathResource(serviceAccountKeyPath).getInputStream();
//                }
//
//                // Firebase Options 설정
//                FirebaseOptions options = FirebaseOptions.builder()
//                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                        .build();
//
//                // Firebase App 초기화
//                FirebaseApp.initializeApp(options);
//                log.info("Firebase 초기화 완료");
//            } else {
//                log.info("Firebase는 이미 초기화되어 있습니다.");
//            }
//
//        } catch (IOException e) {
//            log.error("Firebase 초기화 실패: Service Account Key 파일을 찾을 수 없습니다. - {}", e.getMessage());
//            throw new NotificationException(ErrorMessage.NOTIFICATION_FIREBASE_INIT_FAILED);
//        } catch (Exception e) {
//            log.error("Firebase 초기화 중 예상치 못한 오류 발생: {}", e.getMessage());
//            throw new NotificationException(ErrorMessage.NOTIFICATION_FIREBASE_INIT_FAILED);
//        }
//    }
//}
