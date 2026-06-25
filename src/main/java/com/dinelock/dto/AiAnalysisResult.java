package com.dinelock.dto;

// Modern Java Record to hold the structured JSON data returning from Gemini
public record AiAnalysisResult(
        int taste,
        int speed,
        int price,
        int ambience,
        String sentiment,
        String summary
) {}