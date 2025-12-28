package tr.edu.kalyon.nur.university.controller.dto;

import java.util.List;

public record OutfitCandidateDto(
        Long outfitId,
        Double score,
        List<ClotheCandidateDto> clothes
) {}
