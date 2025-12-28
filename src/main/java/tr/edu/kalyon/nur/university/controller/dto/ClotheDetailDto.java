package tr.edu.kalyon.nur.university.controller.dto;

public record ClotheDetailDto(
        Long clotheId,
        String category,
        String color,
        String style,
        String imageContentType,
        String imageBase64
) {}
