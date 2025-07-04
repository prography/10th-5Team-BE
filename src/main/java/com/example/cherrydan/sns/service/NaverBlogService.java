package com.example.cherrydan.sns.service;

import com.example.cherrydan.sns.domain.SnsConnection;
import com.example.cherrydan.sns.domain.SnsPlatform;
import com.example.cherrydan.sns.repository.SnsConnectionRepository;
import com.example.cherrydan.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.net.URL;
import com.example.cherrydan.common.exception.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import com.example.cherrydan.user.repository.UserRepository;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverBlogService {
    private final SnsConnectionRepository snsConnectionRepository;
    private final UserRepository userRepository;

    public String verifyAndSave(String code, String blogUrl, User user, SnsPlatform platform) {
        try {
            String rssUrl = getRssUrl(blogUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new URL(rssUrl)));
            String description = feed.getDescription();
            
            if (description != null && description.contains(code)) {
                Optional<SnsConnection> existing = snsConnectionRepository.findByUserAndPlatform(user, platform);
                if (existing.isPresent()) {
                    SnsConnection connection = existing.get();
                    connection.setSnsUrl(blogUrl);
                    snsConnectionRepository.save(connection);
                } else {
                    SnsConnection connection = SnsConnection.builder()
                        .user(user)
                        .platform(platform)
                        .snsUrl(blogUrl)
                        .isActive(true)
                        .build();
                    snsConnectionRepository.save(connection);
                }
                return blogUrl;
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("NaverBlogService.verifyAndSave error for blogUrl={} userId={}", blogUrl, user.getId(), e);
            throw new RuntimeException(ErrorMessage.NAVER_BLOG_INVALID_URL.getMessage());
        }
        return null;
    }

    private String getRssUrl(String blogUrl) {
        if (blogUrl == null || blogUrl.isBlank()) {
            throw new IllegalArgumentException(ErrorMessage.NAVER_BLOG_INVALID_URL.getMessage());
        }

        String url = blogUrl.trim().toLowerCase();
        String domain = url.replaceFirst("^https?://", "");
        if (!domain.startsWith("m.blog.naver.com") && !domain.startsWith("blog.naver.com")) {
            throw new IllegalArgumentException(ErrorMessage.NAVER_BLOG_INVALID_URL.getMessage());
        }

        String id = domain.replaceFirst("^(m\\.)?blog\\.naver\\.com/", "").split("[/?#]")[0];
        if (id.isBlank()) {
            throw new IllegalArgumentException(ErrorMessage.NAVER_BLOG_INVALID_URL.getMessage());
        }
        if (!id.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException(ErrorMessage.NAVER_BLOG_INVALID_ID.getMessage());
        }

        return "https://rss.blog.naver.com/" + id;
    }
} 