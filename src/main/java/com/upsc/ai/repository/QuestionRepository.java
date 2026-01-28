package com.upsc.ai.repository;

import com.upsc.ai.entity.PdfDocument;
import com.upsc.ai.entity.Question;
import com.upsc.ai.entity.Question.DifficultyLevel;
import com.upsc.ai.entity.Subject;
import com.upsc.ai.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySubject(Subject subject);

    List<Question> findByTopic(Topic topic);

    List<Question> findByDifficultyLevel(DifficultyLevel difficultyLevel);

    List<Question> findBySourcePdf(PdfDocument sourcePdf);

    List<Question> findByIsVerified(Boolean isVerified);
}
