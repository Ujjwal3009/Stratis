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

        @Override
        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "options", "subject", "topic" })
        java.util.Optional<Question> findById(Long id);

        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "options", "subject", "topic" })
        List<Question> findBySubject(Subject subject);

        List<Question> findByTopic(Topic topic);

        List<Question> findByDifficultyLevel(DifficultyLevel difficultyLevel);

        List<Question> findBySourcePdf(PdfDocument sourcePdf);

        List<Question> findByIsVerified(Boolean isVerified);

        @org.springframework.data.jpa.repository.Query("SELECT q FROM Question q " +
                        "WHERE (:subjectId IS NULL OR q.subject.id = :subjectId) " +
                        "AND (:topicId IS NULL OR q.topic.id = :topicId)")
        org.springframework.data.domain.Page<Question> findAllPaged(
                        @org.springframework.data.repository.query.Param("subjectId") Long subjectId,
                        @org.springframework.data.repository.query.Param("topicId") Long topicId,
                        org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT q FROM Question q " +
                        "LEFT JOIN UserQuestionAttempt uqa ON q.id = uqa.question.id AND uqa.user.id = :userId " +
                        "WHERE uqa.id IS NULL " +
                        "AND q.subject.id = :subjectId " +
                        "AND (:topicId IS NULL OR q.topic.id = :topicId) " +
                        "AND q.difficultyLevel IN :difficultyLevels " +
                        "AND q.createdSource = :createdSource " +
                        "AND q.isActive = true")
        List<Question> findUnseenQuestions(
                        @org.springframework.data.repository.query.Param("userId") Long userId,
                        @org.springframework.data.repository.query.Param("subjectId") Long subjectId,
                        @org.springframework.data.repository.query.Param("topicId") Long topicId,
                        @org.springframework.data.repository.query.Param("difficultyLevels") List<DifficultyLevel> difficultyLevels,
                        @org.springframework.data.repository.query.Param("createdSource") String createdSource,
                        org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT COUNT(q) FROM Question q " +
                        "WHERE q.subject.id = :subjectId " +
                        "AND (:topicId IS NULL OR q.topic.id = :topicId) " +
                        "AND q.difficultyLevel IN :difficultyLevels " +
                        "AND q.isActive = true")
        long countAvailableQuestions(
                        @org.springframework.data.repository.query.Param("subjectId") Long subjectId,
                        @org.springframework.data.repository.query.Param("topicId") Long topicId,
                        @org.springframework.data.repository.query.Param("difficultyLevels") List<DifficultyLevel> difficultyLevels);

        java.util.Optional<Question> findByNormalizedHash(String normalizedHash);
}
