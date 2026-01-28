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
import java.util.List;

@Service
public class GeminiAiService {

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    @Value("${GEMINI_MODEL:gemini-1.5-pro}")
    private String modelName;

    @Value("${GOOGLE_CLOUD_PROJECT_ID:}")
    private String projectId;

    @Value("${GOOGLE_CLOUD_LOCATION:us-central1}")
    private String location;

    private VertexAI vertexAI;
    private GenerativeModel model;
    private final Gson gson = new Gson();

    @PostConstruct
    public void init() {
        // Note: For Google AI Studio key, we might need a different client or use HTTP
        // directly.
        // Vertex AI SDK usually expects GCP credentials.
        // If using Gemini API Key directly, we might need to use the Google AI SDK or
        // REST.
        // Let's assume GCP environment for now as per implementation plan dependencies.
        if (projectId != null && !projectId.isEmpty()) {
            this.vertexAI = new VertexAI(projectId, location);
            this.model = new GenerativeModel(modelName, vertexAI);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (vertexAI != null) {
            vertexAI.close();
        }
    }

    public List<ParsedQuestion> parseQuestions(String pdfText) {
        if (model == null) {
            // Fallback for local dev if key is provided but no project id
            return mockParseQuestions(pdfText);
        }

        String prompt = buildQuestionExtractionPrompt(pdfText);

        try {
            GenerateContentResponse response = model.generateContent(prompt);
            String output = ResponseHandler.getText(response);

            // Extract JSON from response (Gemini sometimes wraps in ```json ... ```)
            String jsonPart = extractJson(output);

            Type listType = new TypeToken<ArrayList<ParsedQuestion>>() {
            }.getType();
            return gson.fromJson(jsonPart, listType);
        } catch (IOException e) {
            throw new BusinessException("Error calling Gemini AI: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("Error parsing Gemini response: " + e.getMessage());
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
        System.out.println("Gemini AI Mock: Received text of length " + text.length());
        return new ArrayList<>();
    }
}
