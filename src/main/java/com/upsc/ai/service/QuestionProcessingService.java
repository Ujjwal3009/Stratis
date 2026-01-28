package com.upsc.ai.service;

import com.upsc.ai.dto.ParsedQuestion;
import com.upsc.ai.dto.ProcessingResult;
import com.upsc.ai.dto.QuestionDTO;
import com.upsc.ai.entity.*;
import com.upsc.ai.exception.BusinessException;
import com.upsc.ai.repository.PdfDocumentRepository;
import com.upsc.ai.repository.QuestionRepository;
import com.upsc.ai.repository.SubjectRepository;
import com.upsc.ai.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionProcessingService {

    @Autowired
    private PdfTextExtractorService pdfTextExtractor;

    @Autowired
    private GeminiAiService geminiAiService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private PdfDocumentRepository pdfDocumentRepository;

    @Autowired
    private PdfChunkService pdfChunkService;

    @Transactional
    public ProcessingResult processPdf(Long pdfId, User user) {
        PdfDocument pdf = pdfDocumentRepository.findById(pdfId)
                .orElseThrow(() -> new BusinessException("PDF not found: " + pdfId));

        pdf.setStatus(PdfDocument.DocumentStatus.PROCESSING);
        pdfDocumentRepository.save(pdf);

        try {
            // 1. Extract text
            String text = pdfTextExtractor.extractText(pdf.getFilePath());

            // 2. [NEW] Create chunks for context-aware generation
            pdfChunkService.createChunks(pdf, text);

            // 3. Parse questions with Gemini
            List<ParsedQuestion> parsedQuestions = geminiAiService.parseQuestions(text);

            // 3. Save questions
            List<Question> savedQuestions = new ArrayList<>();
            for (ParsedQuestion pq : parsedQuestions) {
                Question question = mapToEntity(pq, pdf, user);
                savedQuestions.add(questionRepository.save(question));
            }

            pdf.setStatus(PdfDocument.DocumentStatus.PROCESSED);
            pdfDocumentRepository.save(pdf);

            return new ProcessingResult(
                    true,
                    savedQuestions.size(),
                    "Successfully extracted " + savedQuestions.size() + " questions",
                    savedQuestions.stream().map(QuestionDTO::fromEntity).collect(Collectors.toList()));

        } catch (Exception e) {
            pdf.setStatus(PdfDocument.DocumentStatus.FAILED);
            pdfDocumentRepository.save(pdf);
            throw new BusinessException("Extraction failed: " + e.getMessage());
        }
    }

    private Question mapToEntity(ParsedQuestion pq, PdfDocument pdf, User user) {
        Question q = new Question();
        q.setQuestionText(pq.getQuestionText());
        q.setQuestionType(Question.QuestionType.valueOf(pq.getQuestionType()));
        q.setDifficultyLevel(Question.DifficultyLevel.valueOf(pq.getDifficultyLevel()));
        q.setExplanation(pq.getExplanation());
        q.setSourcePdf(pdf);
        q.setCreatedBy(user);
        q.setIsVerified(false);

        // Map subject
        if (pq.getSubject() != null) {
            Subject subject = subjectRepository.findByNameIgnoreCase(pq.getSubject())
                    .orElseGet(() -> {
                        Subject s = new Subject();
                        s.setName(pq.getSubject());
                        return subjectRepository.save(s);
                    });
            q.setSubject(subject);

            // Map topic
            if (pq.getTopic() != null) {
                Topic topic = topicRepository.findBySubjectAndNameIgnoreCase(subject, pq.getTopic())
                        .orElseGet(() -> {
                            Topic t = new Topic();
                            t.setName(pq.getTopic());
                            t.setSubject(subject);
                            return topicRepository.save(t);
                        });
                q.setTopic(topic);
            }
        }

        // Map options
        if (pq.getOptions() != null) {
            List<QuestionOption> options = pq.getOptions().stream().map(po -> {
                QuestionOption o = new QuestionOption();
                o.setOptionText(po.getText());
                o.setIsCorrect(po.getIsCorrect());
                o.setOptionOrder(po.getOrder());
                o.setQuestion(q);
                return o;
            }).collect(Collectors.toList());
            q.setOptions(options);
        }

        return q;
    }
}
