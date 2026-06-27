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

            // Prompt Engineering: Gemini'yi KESİNLİKLE sadece JSON dönmeye zorluyoruz
            String prompt = "Analyze this restaurant review and return a JSON object with strictly these keys: " +
                    "'taste' (1-5), 'speed' (1-5), 'price' (1-5), 'ambience' (1-5), 'sentiment' (POSITIVE, NEGATIVE, or NEUTRAL), and 'summary' (a 1 sentence summary). " +
                    "Return ONLY valid JSON without any markdown or code blocks. Review: " + reviewText;

            // JSON gövdesinin bozulmaması için tırnak işaretlerini kaçış (escape) karakteriyle koruyoruz
            String safePrompt = prompt.replace("\"", "\\\"");

            String requestBody = """
                    {
                        "contents": [{
                            "parts": [{"text": "%s"}]
                        }]
                    }
                    """.formatted(safePrompt);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            log.info("Yorum çok kriterli analiz için Gemini AI motoruna gönderiliyor...");
            String response = restTemplate.postForObject(url, request, String.class);

            // Google Gemini'nin iç içe geçmiş JSON yanıtını ayrıştırıyoruz (Parsing)
            JsonNode rootNode = objectMapper.readTree(response);
            String aiResponseText = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Güvenlik Kontrolü: Gemini yanlışlıkla Markdown kodu (```json) eklerse temizliyoruz
            if(aiResponseText.startsWith("```json")) {
                aiResponseText = aiResponseText.replace("```json", "").replace("```", "").trim();
            }

            return objectMapper.readValue(aiResponseText, AiAnalysisResult.class);

        } catch (Exception e) {
            log.error("Gemini API Hatası: ", e);
            // API çökerse veya kota dolarsa, uygulamanın çökmemesi için null dönüyoruz
            return null;
        }
    }
}