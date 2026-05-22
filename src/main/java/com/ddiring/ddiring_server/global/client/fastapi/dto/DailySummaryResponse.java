package com.ddiring.ddiring_server.global.client.fastapi.dto;

import java.util.List;

public record DailySummaryResponse(
        String summary,
        List<String> highlights
) {}
