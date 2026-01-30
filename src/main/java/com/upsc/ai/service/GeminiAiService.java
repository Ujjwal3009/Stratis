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
import org.springframework.beans.factory.annotation.Autowired;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Map;
import java.util.HashMap;

import com.upsc.ai.repository.UserTokenUsageRepository;
import com.upsc.ai.entity.UserTokenUsage;
import com.upsc.ai.entity.User;

@Service
public class GeminiAiService {
    @Autowired
    private UserTokenUsageRepository tokenUsageRepository;

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

    public List<ParsedQuestion> parseQuestions(String pdfText, User user) {
        String prompt = buildQuestionExtractionPrompt(pdfText);
        String output;

        if (model != null) {
            try {
                GenerateContentResponse response = model.generateContent(prompt);
                output = ResponseHandler.getText(response);
                // Extract usage metadata if available (Vertex AI client)
                logUsage(user, modelName, output.length() / 4, 100, "PDF_PARSING");
            } catch (IOException e) {
                throw new BusinessException("Error calling Vertex AI: " + e.getMessage());
            }
        } else if (apiKey != null && !apiKey.isEmpty()) {
            output = callDirectApi(prompt, user, "PDF_PARSING");
        } else {
            return mockParseQuestions(pdfText);
        }

        return parseJsonResponse(output);
    }

    @CircuitBreaker(name = "geminiAi", fallbackMethod = "generateQuestionsFallback")
    public List<ParsedQuestion> generateQuestions(String subject, String topic, String minDifficulty, int count,
            String context, User user) {
        String prompt = buildQuestionGenerationPrompt(subject, topic, minDifficulty, count, context);
        String output;

        if (model != null) {
            try {
                System.out.println("Using Vertex AI Model for generation");
                GenerateContentResponse response = model.generateContent(prompt);
                output = ResponseHandler.getText(response);
            } catch (IOException e) {
                System.err.println("Vertex AI failed: " + e.getMessage());
                throw new BusinessException("Error calling Vertex AI: " + e.getMessage());
            }
        } else if (apiKey != null && !apiKey.isEmpty()) {
            try {
                System.out.println("Using Direct REST API for generation with Key ending in: "
                        + (apiKey.length() > 4 ? apiKey.substring(apiKey.length() - 4) : "****"));
                output = callDirectApi(prompt, user, "QUESTION_GENERATION");
            } catch (Exception e) {
                System.err.println("Gemini API failed with exception: " + e.getMessage());
                e.printStackTrace();
                System.out.println("Switching to static fallback questions...");
                return loadStaticQuestions();
            }
        } else {
            System.out.println("No API Key or Model configured. Using Mock.");
            return mockParseQuestions("Generation for " + subject);
        }

        try {
            return parseJsonResponse(output);
        } catch (Exception e) {
            System.err.println("Error parsing JSON response. Using static fallback questions.");
            return loadStaticQuestions();
        }
    }

    private List<ParsedQuestion> loadStaticQuestions() {
        try {
            org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource(
                    "static_questions.json");
            String json = new String(resource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            Type listType = new TypeToken<ArrayList<ParsedQuestion>>() {
            }.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            System.err.println("Failed to load static fallback questions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ParsedQuestion> generateQuestionsFallback(String subject, String topic, String minDifficulty, int count,
            String context, User user, Throwable t) {
        System.err.println("Circuit Breaker triggered for Gemini AI: " + t.getMessage());
        return loadStaticQuestions();
    }

    public String generateAnalysisInsights(String analysisContext, User user) {
        String prompt = buildAnalysisPrompt(analysisContext);
        if (model != null) {
            try {
                GenerateContentResponse response = model.generateContent(prompt);
                String output = ResponseHandler.getText(response);
                logUsage(user, modelName, analysisContext.length() / 4, output.length() / 4, "ANALYSIS");
                return output;
            } catch (IOException e) {
                throw new BusinessException("Error calling Vertex AI: " + e.getMessage());
            }
        } else if (apiKey != null && !apiKey.isEmpty()) {
            return callDirectApi(prompt, user, "ANALYSIS");
        } else {
            return "{\"diagnosticSummary\": \"Mock analysis: You performed well in History but need to focus on timing for Economy.\", \"studyNotes\": \"Key concept: Fiscal policy impact on inflation...\", \"strengthWeaknessPairs\": [{\"point\": \"Ancient History recall\", \"strategy\": \"Practice mapping sites to periods.\"}], \"mistakeCategorization\": [{\"questionId\": 1, \"type\": \"SILLY_MISTAKE\", \"reason\": \"Fast response on easy question\"}]}";
        }
    }

    private String buildAnalysisPrompt(String context) {
        return """
                You are a senior UPSC mentor and performance analyst.
                Analyze the following test results for a UPSC aspirant and provide a deep diagnostic report.

                DATA TO ANALYZE:
                """ + context
                + """

                        YOUR GOAL:
                        1. Identify patterns in mistakes (Silly vs Knowledge Gap vs Logical).
                        2. Provide 'Study Notes': A concise bulleted summary of the core concepts tested in the test that the user should review.
                        3. Strength vs Weakness: High-level actionable points.

                        RETURN FORMAT (JSON ONLY):
                        {
                            "diagnosticSummary": "A professional paragraph summarizing performance and behavior.",
                            "studyNotes": "Markdown formatted bullet points of conceptual highlights from the test.",
                            "strengthWeaknessPairs": [
                                {"point": "Strength or Weakness area", "strategy": "Actionable advice"}
                            ],
                            "mistakeCategorization": [
                                {"questionId": 123, "type": "Knowledge Gap | Silly Mistake | Logical Error", "reason": "Brief explanation why"}
                            ]
                        }

                        Return ONLY the JSON object.
                        """;
    }

    @SuppressWarnings("unchecked")
    private String callDirectApi(String prompt, User user, String featureArea) {
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
                // Tracking Usage
                Map<String, Object> usage = (Map<String, Object>) response.get("usageMetadata");
                if (usage != null && user != null) {
                    logUsage(user, modelName,
                            (Integer) usage.get("promptTokenCount"),
                            (Integer) usage.get("candidatesTokenCount"),
                            featureArea);
                }

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

    private void logUsage(User user, String model, Integer prompt, Integer completion, String area) {
        try {
            UserTokenUsage usage = new UserTokenUsage();
            usage.setUser(user);
            usage.setModelName(model);
            usage.setPromptTokens(prompt != null ? prompt : 0);
            usage.setCompletionTokens(completion != null ? completion : 0);
            usage.setTotalTokens(usage.getPromptTokens() + usage.getCompletionTokens());
            usage.setFeatureArea(area);
            tokenUsageRepository.save(usage);
        } catch (Exception e) {
            System.err.println("Failed to log token usage: " + e.getMessage());
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

    private String buildQuestionGenerationPrompt(String subject, String topic, String minDifficulty, int count,
            String context) {
        String basePrompt = String.format(
                """
                        You are a senior UPSC Civil Services Preliminary Examination (Prelims) expert.
                        Generate %d UPSC-style MCQ questions for the following topic.
                        Subject: %s
                        Topic: %s
                        Target Difficulty: %s

                        CRITICAL UPSC QUESTION DESIGN GUIDELINES:
                        1. DYNAMIC STRUCTURE: For HARD and MEDIUM questions, use multi-statement formats followed by "Select the correct answer using the code given below:".
                           Example:
                           Statement 1: [Concept A]
                           Statement 2: [Concept B]
                           Which of the statements given above is/are correct?
                           (a) 1 only (b) 2 only (c) Both 1 and 2 (d) Neither 1 nor 2
                        2. DEPTH: Avoid simple factual recall. Focus on conceptual clarity, causal relationships, and analytic reasoning.
                        3. DISTRACTORS: Options should be plausible and challenging. Use "Pair matching" or "Only one pair, Only two pairs" style distractor if appropriate.
                        4. LANGUAGE: Use professional, academic, and complex terminology as found in official UPSC GS papers.
                        5. EXPLANATION: Provide a "Rationale" explaining why each statement is correct or incorrect.

                        """,
                count, subject, topic, minDifficulty);

        if (context != null && !context.isBlank()) {
            basePrompt += String.format(
                    """
                            CONTEXTUAL SOURCE MATERIAL:
                            The questions MUST be framed based on the information provided in the following excerpt:
                            ---
                            %s
                            ---

                            Ensure the questions test understanding of this specific content while maintaining the UPSC level of analytical rigor.

                            """,
                    context);
        } else {
            basePrompt += "Use your vast knowledge database to generate original questions corresponding to the UPSC syllabus for this topic.\n\n";
        }

        basePrompt += String.format("""
                FORMAT EACH QUESTION AS A JSON OBJECT:
                {
                    "questionText": "The full text of the question including statements",
                    "questionType": "MCQ",
                    "difficultyLevel": "%s",
                    "subject": "%s",
                    "topic": "%s",
                    "explanation": "Detailed professional rationale",
                    "options": [
                        {"text": "Option A (or 1 only)", "isCorrect": false, "order": 1},
                        {"text": "Option B (or 2 only)", "isCorrect": true, "order": 2},
                        ...
                    ]
                }

                Return ONLY a JSON array of %d these objects. No introduction or closing text.
                """, minDifficulty, subject, topic, count);

        return basePrompt;
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
