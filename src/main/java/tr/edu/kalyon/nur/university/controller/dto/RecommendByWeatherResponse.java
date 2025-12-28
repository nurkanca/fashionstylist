package tr.edu.kalyon.nur.university.controller.dto;

import java.util.List;

public record RecommendByWeatherResponse(
        String season,
        double temperature,
        List<Long> recommendedOutfitIds
) {}
