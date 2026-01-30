package com.upsc.ai.repository;

import com.upsc.ai.entity.UserTokenUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTokenUsageRepository extends JpaRepository<UserTokenUsage, Long> {
    List<UserTokenUsage> findByUser_IdOrderByUsedAtDesc(Long userId);
}
