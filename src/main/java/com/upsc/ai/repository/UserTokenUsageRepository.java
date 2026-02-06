package com.upsc.ai.repository;

import com.upsc.ai.entity.UserTokenUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTokenUsageRepository extends JpaRepository<UserTokenUsage, Long> {
    List<UserTokenUsage> findByUser_IdOrderByUsedAtDesc(Long userId);

    @Query("SELECT SUM(ut.totalTokens) FROM UserTokenUsage ut")
    Long getTotalTokensUsed();

    @Query("SELECT ut.featureArea, SUM(ut.totalTokens) FROM UserTokenUsage ut GROUP BY ut.featureArea")
    List<Object[]> getUsageByFeatureArea();
}
