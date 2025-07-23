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
import java.io.*;

/**
 * Firebase Configuration
 * Firebase Admin SDK 초기화 및 설정을 담당하는 클래스
 *
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${fcm.service-account-key-path}")
    private String serviceAccountKeyPath;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getServiceAccountStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase 초기화 완료");
            } else {
                log.info("Firebase는 이미 초기화되어 있습니다.");
            }

        } catch (IOException e) {
            log.error("Firebase 초기화 실패: Service Account Key 파일을 찾을 수 없습니다. - {}", e.getMessage());
            throw new NotificationException(ErrorMessage.NOTIFICATION_FIREBASE_INIT_FAILED);
        } catch (Exception e) {
            log.error("Firebase 초기화 중 예상치 못한 오류 발생: {}", e.getMessage());
            throw new NotificationException(ErrorMessage.NOTIFICATION_FIREBASE_INIT_FAILED);
        }
    }

    private InputStream getServiceAccountStream() throws IOException {
        log.info("Firebase 서비스 계정 키 경로: {}", serviceAccountKeyPath);

        // classpath 경로인 경우
        if (serviceAccountKeyPath.startsWith("classpath:")) {
            String path = serviceAccountKeyPath.substring("classpath:".length());
            log.info("classpath에서 Firebase 키 파일 로드: {}", path);
            return new ClassPathResource(path).getInputStream();
        }
        // 절대 경로인 경우 (/ 로 시작)
        else if (serviceAccountKeyPath.startsWith("/")) {
            File keyFile = new File(serviceAccountKeyPath);
            if (!keyFile.exists()) {
                throw new FileNotFoundException("Firebase 키 파일을 찾을 수 없습니다: " + serviceAccountKeyPath);
            }
            log.info("절대 경로에서 Firebase 키 파일 로드: {}", serviceAccountKeyPath);
            return new FileInputStream(keyFile);
        }
        // 상대 경로는 classpath로 처리
        else {
            log.info("상대 경로를 classpath로 처리: {}", serviceAccountKeyPath);
            return new ClassPathResource(serviceAccountKeyPath).getInputStream();
        }
    }
}

