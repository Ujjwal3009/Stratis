package com.upsc.ai.service;

import com.upsc.ai.entity.*;
import com.upsc.ai.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataImportService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public int importQuestionsFromCsv(String filePath) throws Exception {
        List<List<String>> records = parseCsv(filePath);
        if (records.isEmpty())
            return 0;

        // Skip header
        records.remove(0);

        Map<String, Subject> subjectCache = new HashMap<>();
        Map<String, Topic> topicCache = new HashMap<>();

        int count = 0;

        // fetch existing to cache
        subjectRepository.findAll().forEach(s -> subjectCache.put(s.getName().toLowerCase(), s));
        topicRepository.findAll().forEach(t -> topicCache.put(t.getName().toLowerCase(), t));

        User adminUser = userRepository.findAll().stream().findFirst().orElse(null);

        for (List<String> record : records) {
            if (record.size() < 14)
                continue; // Basic validation

            // Columns:
            // 0:Paper, 1:Passage, 2:Question, 3:OptA, 4:OptB, 5:OptC, 6:OptD,
            // 7:Correct(A/B/C/D), 8:Expl, 9:Subject, 10:Topic, 11:Year, 12:Img, 13:Diff,
            // 14:Cognitive

            String paper = record.get(0).trim();
            String passage = record.get(1).trim();
            String qText = record.get(2).trim();
            String optA = record.get(3).trim();
            String optB = record.get(4).trim();
            String optC = record.get(5).trim();
            String optD = record.get(6).trim();
            String correctAns = record.get(7).trim().toUpperCase();
            String explanation = record.get(8).trim();
            String subjectName = record.get(9).trim();
            String topicName = record.get(10).trim();
            String yearStr = record.get(11).trim();
            String imgUrl = record.get(12).trim();
            String difficulty = record.get(13).trim().toUpperCase();
            String cognitive = record.size() > 14 ? record.get(14).trim() : "";

            // Mapping
            if (subjectName.isEmpty())
                subjectName = "General";
            Subject subject = subjectCache.computeIfAbsent(subjectName.toLowerCase(), k -> {
                Subject s = new Subject();
                s.setName(trimName(k));
                return subjectRepository.save(s);
            });

            Topic topic = null;
            if (!topicName.isEmpty()) {
                // Correctly use findBySubjectAndNameIgnoreCase
                topic = topicRepository.findBySubjectAndNameIgnoreCase(subject, trimName(topicName))
                        .orElseGet(() -> {
                            Topic t = new Topic();
                            t.setName(trimName(topicName));
                            t.setSubject(subject);
                            return topicRepository.save(t);
                        });
            }

            Question q = new Question();
            q.setPaper(paper);
            q.setPassage(passage.isEmpty() ? null : passage);
            q.setQuestionText(qText);
            q.setExplanation(explanation);
            q.setSubject(subject);
            q.setTopic(topic);
            q.setCreatedBy(adminUser);
            q.setCreatedSource("PYQ");
            q.setYear(yearStr.matches("\\d+") ? Integer.parseInt(yearStr) : null);
            q.setImageUrl(imgUrl.isEmpty() ? null : imgUrl);
            q.setIsVerified(true);
            q.setCognitiveLevel(cognitive);

            // Difficulty mapping
            try {
                if (difficulty.contains("EASY"))
                    q.setDifficultyLevel(Question.DifficultyLevel.EASY);
                else if (difficulty.contains("HARD"))
                    q.setDifficultyLevel(Question.DifficultyLevel.HARD);
                else
                    q.setDifficultyLevel(Question.DifficultyLevel.MEDIUM);
            } catch (Exception e) {
                q.setDifficultyLevel(Question.DifficultyLevel.MEDIUM);
            }

            q.setQuestionType(Question.QuestionType.MCQ);

            // Hash for deduplication
            q.generateHash();
            // Check if exists
            if (questionRepository.findByNormalizedHash(q.getNormalizedHash()).isPresent()) {
                continue; // Skip duplicate
            }

            // Save Question First (Cascade Fix Pattern)
            q = questionRepository.save(q);

            // Options
            List<QuestionOption> options = new ArrayList<>();
            options.add(createOption(q, optA, "A".equals(correctAns), 1));
            options.add(createOption(q, optB, "B".equals(correctAns), 2));
            options.add(createOption(q, optC, "C".equals(correctAns), 3));
            options.add(createOption(q, optD, "D".equals(correctAns), 4));

            q.setOptions(options);
            questionRepository.save(q);
            count++;
        }
        return count;
    }

    private String trimName(String s) {
        if (s == null)
            return "";
        // Capitalize first letter logic if needed, or just return trimmed
        String res = s.trim();
        if (res.length() > 0)
            return res.substring(0, 1).toUpperCase() + res.substring(1);
        return res;
    }

    private QuestionOption createOption(Question q, String text, boolean isCorrect, int order) {
        QuestionOption o = new QuestionOption();
        o.setOptionText(text);
        o.setIsCorrect(isCorrect);
        o.setOptionOrder(order);
        o.setQuestion(q);
        return o;
    }

    // Custom CSV Parser to handle multiline and quotes
    public List<List<String>> parseCsv(String filePath) throws IOException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder currentField = new StringBuilder();
            List<String> currentRecord = new ArrayList<>();
            boolean inQuotes = false;
            int c;
            while ((c = br.read()) != -1) {
                char ch = (char) c;
                if (inQuotes) {
                    if (ch == '\"') {
                        // Check next char
                        br.mark(1);
                        int next = br.read();
                        if (next == '\"') {
                            // Escaped quote
                            currentField.append('\"');
                        } else {
                            // End quote
                            inQuotes = false;
                            br.reset();
                        }
                    } else {
                        currentField.append(ch);
                    }
                } else {
                    if (ch == '\"') {
                        inQuotes = true;
                    } else if (ch == ',') {
                        currentRecord.add(currentField.toString());
                        currentField.setLength(0);
                    } else if (ch == '\n' || ch == '\r') {
                        // Handle formatting \r\n
                        br.mark(1);
                        int next = br.read();
                        if (ch == '\r' && next == '\n') {
                            // consume \n
                        } else {
                            br.reset();
                        }

                        // End of record
                        // Add last field
                        currentRecord.add(currentField.toString());
                        currentField.setLength(0);

                        // Only add non-empty records
                        if (!currentRecord.isEmpty() && (currentRecord.size() > 1 || !currentRecord.get(0).isEmpty())) {
                            records.add(new ArrayList<>(currentRecord));
                        }
                        currentRecord.clear();
                    } else {
                        currentField.append(ch);
                    }
                }
            }
            // Add last record if exists
            if (!currentRecord.isEmpty() || currentField.length() > 0) {
                currentRecord.add(currentField.toString());
                records.add(currentRecord);
            }
        }
        return records;
    }
}
