package com.backend.nutri_predic.conocimiento.gemini.client;

import java.util.List;

public record GeminiResponseDiagnostics(
        Integer httpStatus,
        String contentType,
        int candidateCount,
        int partCount,
        List<String> partTypes,
        boolean hasText,
        int textLength,
        boolean startsWithBrace,
        boolean startsWithBracket,
        boolean hasMarkdownFence,
        String finishReason,
        String parseStage,
        String apiErrorStatus,
        Integer apiErrorCode) {}
