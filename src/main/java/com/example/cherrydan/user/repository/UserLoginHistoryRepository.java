package com.example.cherrydan.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cherrydan.user.domain.UserLoginHistory;

public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> {
}
