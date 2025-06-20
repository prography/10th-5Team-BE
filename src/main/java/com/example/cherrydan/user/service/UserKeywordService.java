package com.example.cherrydan.user.service;

import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.UserKeyword;
import com.example.cherrydan.user.repository.UserKeywordRepository;
import com.example.cherrydan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserKeywordService {
    private final UserKeywordRepository userKeywordRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addKeyword(Long userId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (userKeywordRepository.findByUserIdAndKeyword(userId, keyword).isPresent()) {
            throw new IllegalStateException("이미 등록된 키워드입니다.");
        }
        userKeywordRepository.save(UserKeyword.builder().user(user).keyword(keyword).build());
    }

    @Transactional(readOnly = true)
    public List<UserKeyword> getKeywords(Long userId) {
        return userKeywordRepository.findByUserId(userId);
    }

    @Transactional
    public void removeKeyword(Long userId, String keyword) {
        userKeywordRepository.deleteByUserIdAndKeyword(userId, keyword);
    }
} 