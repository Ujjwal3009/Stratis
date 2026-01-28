package com.upsc.ai.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.upsc.ai.dto.ParsedQuestion;
import com.upsc.ai.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;

@Service
public class GeminiAiService {

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    @Value("${GEMINI_MODEL:gemini-1.5-flash}")
    private String modelName;

    @Value("${GOOGLE_CLOUD_PROJECT_ID:}")
    private String projectId;

    @Value("${GOOGLE_CLOUD_LOCATION:us-central1}")
    private String location;

    private VertexAI vertexAI;
    private GenerativeModel model;
    private final Gson gson = new Gson();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        if (projectId != null && !projectId.isEmpty()) {
            this.vertexAI = new VertexAI(projectId, location);
            this.model = new GenerativeModel(modelName, vertexAI);
        } else if (apiKey != null && !apiKey.isEmpty()) {
            System.out.println("Gemini AI Service: Using API Key with direct REST calls. Model: " + modelName);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (vertexAI != null) {
            vertexAI.close();
        }
    }

    public List<ParsedQuestion> parseQuestions(String pdfText) {
        String prompt = buildQuestionExtractionPrompt(pdfText);
        String output;

        if (model != null) {
            try {
                GenerateContentResponse response = model.generateContent(prompt);
                output = ResponseHandler.getText(response);
            } catch (IOException e) {
                throw new BusinessException("Error calling Vertex AI: " + e.getMessage());
            }
        } else if (apiKey != null && !apiKey.isEmpty()) {
            output = callDirectApi(prompt);
        } else {
            return mockParseQuestions(pdfText);
        }

        return parseJsonResponse(output);
    }

    public List<ParsedQuestion> generateQuestions(String subject, String topic, String minDifficulty, int count) {
        String prompt = buildQuestionGenerationPrompt(subject, topic, minDifficulty, count);
        String output;

        if (model != null) {
            try {
                GenerateContentResponse response = model.generateContent(prompt);
                output = ResponseHandler.getText(response);
            } catch (IOException e) {
                throw new BusinessException("Error calling Vertex AI: " + e.getMessage());
            }
        } else if (apiKey != null && !apiKey.isEmpty()) {
            output = callDirectApi(prompt);
        } else {
            return mockParseQuestions("Generation for " + subject);
        }

        return parseJsonResponse(output);
    }

    @SuppressWarnings("unchecked")
    private String callDirectApi(String prompt) {
        String url = String.format("https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s",
                modelName, apiKey);

        Map<String, Object> body = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);
        body.put("contents", contents);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> resContent = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> resParts = (List<Map<String, Object>>) resContent.get("parts");
                    return (String) resParts.get(0).get("text");
                }
            }
            throw new BusinessException("Empty response from Gemini API");
        } catch (Exception e) {
            throw new BusinessException("Error calling Gemini direct API: " + e.getMessage());
        }
    }

    private String buildQuestionExtractionPrompt(String text) {
        return """
                You are an expert UPSC content creator. Extract all UPSC questions from the following text accurately.
                For each question found, provide a JSON object with the following structure:
                {
                    "questionText": "The text of the question",
                    "questionType": "MCQ" or "SUBJECTIVE" or "TRUE_FALSE",
                    "difficultyLevel": "EASY" or "MEDIUM" or "HARD",
                    "subject": "e.g. History, Polity, Economy",
                    "topic": "The specific topic",
                    "explanation": "Brief explanation of the answer",
                    "options": [
                        {"text": "Option A", "isCorrect": false, "order": 1},
                        {"text": "Option B", "isCorrect": true, "order": 2}
                    ]
                }

                Return ONLY a JSON array of these objects. No extra commentary.

                Text to process:
                """ + text;
    }

    private String buildQuestionGenerationPrompt(String subject, String topic, String minDifficulty, int count) {
        return String.format("""
                You are an expert UPSC content creator. Generate %d HIGH-QUALITY MCQ questions for the following topic.
                Subject: %s
                Topic: %s
                Minimum Difficulty Level: %s

                CRITICAL GUIDELINES:
                1. The questions must be at the specified difficulty level (%s) OR HARDER.
                2. If the level is MEDIUM, do NOT generate EASY questions.
                3. If the level is HARD, generate only advanced, multi-statement questions typical of UPSC CSE.
                4. Each question must have exactly 4 options with one correct answer.
                5. Provide a detailed explanation for each answer.

                Format each question as a JSON object:
                {
                    "questionText": "The text of the question",
                    "questionType": "MCQ",
                    "difficultyLevel": "%s", (or HARD if requested level is MEDIUM, etc.)
                    "subject": "%s",
                    "topic": "%s",
                    "explanation": "Detailed explanation",
                    "options": [
                        {"text": "Option 1", "isCorrect": false, "order": 1},
                        {"text": "Option 2", "isCorrect": true, "order": 2},
                        ...
                    ]
                }

                Return ONLY a JSON array of %d these objects.
                """, count, subject, topic, minDifficulty, minDifficulty, minDifficulty, subject, topic, count);
    }

    private List<ParsedQuestion> parseJsonResponse(String output) {
        try {
            String jsonPart = extractJson(output);
            Type listType = new TypeToken<ArrayList<ParsedQuestion>>() {
            }.getType();
            return gson.fromJson(jsonPart, listType);
        } catch (Exception e) {
            throw new BusinessException("Error parsing Gemini response: " + e.getMessage());
        }
    }

    private String extractJson(String output) {
        if (output.contains("```json")) {
            return output.substring(output.indexOf("```json") + 7, output.lastIndexOf("```")).trim();
        } else if (output.contains("```")) {
            return output.substring(output.indexOf("```") + 3, output.lastIndexOf("```")).trim();
        }
        return output.trim();
    }

    private List<ParsedQuestion> mockParseQuestions(String text) {
        // Fallback for when API is not fully configured yet
        System.out.println("Gemini AI Mock: Randomizing questions from mock pool.");
        List<ParsedQuestion> pool = new ArrayList<>();

        // Question 1
        ParsedQuestion q1 = new ParsedQuestion();
        q1.setQuestionText(
                "With reference to the history of ancient India, Bhavabhuti, Hastimalla and Kshemeshvara were famous?");
        q1.setQuestionType("MCQ");
        q1.setDifficultyLevel("HARD");
        q1.setSubject("History");
        q1.setTopic("Ancient India");
        q1.setExplanation(
                "These were famous playwrights in ancient India. Bhavabhuti wrote Mahaviracharita, Uttaramacharita and Malatimadhava.");
        q1.setOptions(List.of(
                new ParsedQuestion.ParsedOption("Jain monks", false, 1),
                new ParsedQuestion.ParsedOption("Playwrights", true, 2),
                new ParsedQuestion.ParsedOption("Temple architects", false, 3),
                new ParsedQuestion.ParsedOption("Philosophers", false, 4)));
        pool.add(q1);

        // Question 2
        ParsedQuestion q2 = new ParsedQuestion();
        q2.setQuestionText("Which one of the following is not a bird?");
        q2.setQuestionType("MCQ");
        q2.setDifficultyLevel("MEDIUM");
        q2.setSubject("Environment");
        q2.setTopic("Fauna");
        q2.setExplanation("Golden Mahseer is a large cyprinid and is considered a fish, not a bird.");
        q2.setOptions(List.of(
                new ParsedQuestion.ParsedOption("Golden Mahseer", true, 1),
                new ParsedQuestion.ParsedOption("Indian Nightjar", false, 2),
                new ParsedQuestion.ParsedOption("Spoonbill", false, 3),
                new ParsedQuestion.ParsedOption("White Ibis", false, 4)));
        pool.add(q2);

        // Question 3
        ParsedQuestion q3 = new ParsedQuestion();
        q3.setQuestionText("The 'Global Financial Stability Report' is prepared by the?");
        q3.setQuestionType("MCQ");
        q3.setDifficultyLevel("EASY");
        q3.setSubject("Economy");
        q3.setTopic("International Institutions");
        q3.setExplanation(
                "The Global Financial Stability Report (GFSR) is a semiannual report by the International Monetary Fund (IMF).");
        q3.setOptions(List.of(
                new ParsedQuestion.ParsedOption("European Central Bank", false, 1),
                new ParsedQuestion.ParsedOption("International Monetary Fund", true, 2),
                new ParsedQuestion.ParsedOption("International Bank for Reconstruction and Development", false, 3),
                new ParsedQuestion.ParsedOption("Organization for Economic Cooperation and Development", false, 4)));
        pool.add(q3);

        // Question 4
        ParsedQuestion q4 = new ParsedQuestion();
        q4.setQuestionText(
                "Which of the following is the correct sequence of the occurrence of the following cities in South-East Asia as one proceeds from South to North?");
        q4.setQuestionType("MCQ");
        q4.setDifficultyLevel("HARD");
        q4.setSubject("Geography");
        q4.setTopic("World Geography");
        q4.setExplanation(
                "The correct order from South to North is: Jakarta (Indonesia), Singapore, Bangkok (Thailand), Hanoi (Vietnam).");
        q4.setOptions(List.of(
                new ParsedQuestion.ParsedOption("Bangkok - Singapore - Jakarta - Hanoi", false, 1),
                new ParsedQuestion.ParsedOption("Jakarta - Singapore - Bangkok - Hanoi", true, 2),
                new ParsedQuestion.ParsedOption("Jakarta - Bangkok - Singapore - Hanoi", false, 3),
                new ParsedQuestion.ParsedOption("Singapore - Jakarta - Bangkok - Hanoi", false, 4)));
        pool.add(q4);

        Collections.shuffle(pool);
        return pool.stream().limit(2).collect(Collectors.toList());
    }
}
