package tr.edu.kalyon.nur.university.model;

import java.util.Set;

public record SaveSurveyRequest(
        Long userId,
        Set<String> favoriteColors,
        Set<String> favoriteStyles,
        String outfitPreference
) {}
