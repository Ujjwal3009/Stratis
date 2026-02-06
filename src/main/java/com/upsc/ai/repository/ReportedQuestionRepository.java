package com.upsc.ai.repository;

import com.upsc.ai.entity.ReportedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportedQuestionRepository extends JpaRepository<ReportedQuestion, Long> {
    List<ReportedQuestion> findByStatus(ReportedQuestion.IssueStatus status);
}
