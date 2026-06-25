package com.dinelock.service;

import com.dinelock.dto.AiAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// İŞTE BÜYÜK DEĞİŞİKLİK: Eski com.fasterxml.jackson yerine yeni tools.jackson kullanıyoruz!

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    public AiAnalysisResult analyzeReview(String reviewText) {
        try {
            String url = apiUrl + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Prompt Engineering: Forcing Gemini to strictly return JSON
            String prompt = "Analyze this restaurant review and return a JSON object with strictly these keys: " +
                    "'taste' (1-5), 'speed' (1-5), 'price' (1-5), 'ambience' (1-5), 'sentiment' (POSITIVE, NEGATIVE, or NEUTRAL), and 'summary' (a 1 sentence summary). " +
                    "Return ONLY valid JSON without any markdown or code blocks. Review: " + reviewText;

            // Escaping quotes to prevent JSON payload corruption
            String safePrompt = prompt.replace("\"", "\\\"");

            String requestBody = """
                    {
                        "contents": [{
                            "parts": [{"text": "%s"}]
                        }]
                    }
                    """.formatted(safePrompt);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            log.info("Sending review to Gemini AI engine for multi-criteria analysis...");
            String response = restTemplate.postForObject(url, request, String.class);

            // Parsing the deeply nested Google Gemini JSON response
            JsonNode rootNode = objectMapper.readTree(response);
            String aiResponseText = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Security Check: Cleaning up markdown if Gemini accidentally includes it
            if(aiResponseText.startsWith("```json")) {
                aiResponseText = aiResponseText.replace("```json", "").replace("```", "").trim();
            }

            return objectMapper.readValue(aiResponseText, AiAnalysisResult.class);

        } catch (Exception e) {
            log.error("Gemini API Error: ", e);
            // Returning a fallback object to prevent application crash if API fails or quota is exceeded
            return new AiAnalysisResult(3, 3, 3, 3, "NEUTRAL", "AI Analysis temporarily unavailable.");
        }
    }
}