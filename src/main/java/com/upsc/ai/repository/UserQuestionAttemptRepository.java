package com.upsc.ai.repository;

import com.upsc.ai.entity.UserQuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQuestionAttemptRepository extends JpaRepository<UserQuestionAttempt, Long> {

    @Query("SELECT distinct uqa.question.id FROM UserQuestionAttempt uqa WHERE uqa.user.id = :userId")
    List<Long> findAttemptedQuestionIdsByUserId(@Param("userId") Long userId);
}
