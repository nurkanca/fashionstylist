package tr.edu.kalyon.nur.university.service;

import org.springframework.stereotype.Service;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;
import tr.edu.kalyon.nur.university.storage.entity.UserSurveyPreference;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PersonalScoreService {

    private final UserSurveyPreferenceService prefService;

    public PersonalScoreService(UserSurveyPreferenceService prefService) {
        this.prefService = prefService;
    }

    public double score(Long userId, List<Clothe> combo) {
        UserSurveyPreference pref = prefService.getOrDefault(userId);

        double colorScore = colorScore(pref.getFavoriteColors(), combo);
        double styleScore = styleScore(pref.getFavoriteStyles(), combo);
        double templateScore = templateScore(pref.getOutfitPreference(), combo);

        double personal = 0.4 * colorScore + 0.4 * styleScore + 0.2 * templateScore;
        return clamp01(personal);
    }

    private double colorScore(Set<String> favoriteColors, List<Clothe> combo) {
        if (favoriteColors == null || favoriteColors.isEmpty()) return 0.5; // neutral

        int total = 0;
        int hit = 0;

        for (Clothe c : combo) {
            if (c.getColor() == null) continue;
            total++;
            String col = c.getColor().toLowerCase(Locale.ROOT);
            if (favoriteColors.contains(col)) hit++;
        }

        if (total == 0) return 0.5;
        return (double) hit / total; // 0..1
    }

    private double styleScore(Set<String> favoriteStyles, List<Clothe> combo) {
        if (favoriteStyles == null || favoriteStyles.isEmpty()) return 0.5;

        int total = 0;
        int hit = 0;

        for (Clothe c : combo) {
            if (c.getStyle() == null) continue;
            total++;
            String st = c.getStyle().toLowerCase(Locale.ROOT);
            if (favoriteStyles.contains(st)) hit++;
        }

        if (total == 0) return 0.5;
        return (double) hit / total;
    }

    private double templateScore(UserSurveyPreference.OutfitPreference pref, List<Clothe> combo) {
        // Determine template from categories
        boolean hasOnePiece = combo.stream().anyMatch(c -> "one_piece".equalsIgnoreCase(c.getCategory()));
        boolean hasTop = combo.stream().anyMatch(c -> "top".equalsIgnoreCase(c.getCategory()));
        boolean hasBottom = combo.stream().anyMatch(c -> "bottom".equalsIgnoreCase(c.getCategory()));

        String template;
        if (hasOnePiece) template = "ONE_PIECE";
        else if (hasTop && hasBottom) template = "TOP_BOTTOM";
        else template = "OTHER";

        if (pref == null || pref == UserSurveyPreference.OutfitPreference.EQUAL) return 0.5;

        if (pref == UserSurveyPreference.OutfitPreference.ONE_PIECE) {
            return template.equals("ONE_PIECE") ? 1.0 : 0.0;
        }
        if (pref == UserSurveyPreference.OutfitPreference.TOP_BOTTOM) {
            return template.equals("TOP_BOTTOM") ? 1.0 : 0.0;
        }
        return 0.5;
    }

    private double clamp01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }
}
