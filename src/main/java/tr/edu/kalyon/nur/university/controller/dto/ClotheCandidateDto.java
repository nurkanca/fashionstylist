package tr.edu.kalyon.nur.university.controller.dto;

import java.util.List;

public record ClotheCandidateDto(
        Long clotheId,
        String category,
        String color,
        String style,

        List<String> seasons
) {}
