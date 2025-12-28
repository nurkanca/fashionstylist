package tr.edu.kalyon.nur.university.controller.dto;

import java.util.List;

public record OutfitDetailResponse(
        Long outfitId,
        Double score,
        Double personalScore,
        List<ClotheDetailDto> clothes
) {}
