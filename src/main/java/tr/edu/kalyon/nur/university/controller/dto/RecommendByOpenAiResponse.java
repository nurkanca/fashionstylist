package tr.edu.kalyon.nur.university.controller.dto;

import java.util.List;

public record RecommendByOpenAiResponse(
        String message,
        List<Long> selectedOutfitIds,
        List<OutfitCandidateDto> outfits
) {}
