package tr.edu.kalyon.nur.university.service;

import org.springframework.stereotype.Component;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class RuleBasedOutfitFilter {

    private static final Set<String> NEUTRALS =
            Set.of("black", "white", "gray", "beige", "cream", "navy_blue", "navy");

    private static boolean isPattern(String c) {
        if (c == null) return false;
        String x = c.toLowerCase(Locale.ROOT);
        return x.equals("patterned") || x.equals("striped");
    }

    private static boolean isNeutral(String c) {
        if (c == null) return false;
        return NEUTRALS.contains(c.toLowerCase(Locale.ROOT));
    }

    /**
     * True -> AI'ye sorulabilir
     * False -> daha baştan ele
     */
    public boolean prefilter(List<Clothe> combo) {
        // one_piece varsa top+bottom olmasın
        boolean hasOnePiece = combo.stream().anyMatch(c -> "one_piece".equalsIgnoreCase(c.getCategory()));
        if (hasOnePiece) {
            boolean hasTop = combo.stream().anyMatch(c -> "top".equalsIgnoreCase(c.getCategory()));
            boolean hasBottom = combo.stream().anyMatch(c -> "bottom".equalsIgnoreCase(c.getCategory()));
            if (hasTop || hasBottom) return false;
        }

        // Pattern clash: 2+ pattern varsa ele
        long patternCount = combo.stream()
                .map(Clothe::getColor)
                .filter(RuleBasedOutfitFilter::isPattern)
                .count();
        if (patternCount >= 2) return false;

        // Eğer hiç neutral yoksa ve 3 parçada da renkler tamamen farklıysa (çok çılgın) ele (opsiyonel)
        // bunu açık bırakmak istemezsen kaldır.
        boolean hasNeutral = combo.stream().map(Clothe::getColor).anyMatch(RuleBasedOutfitFilter::isNeutral);
        if (!hasNeutral && combo.size() >= 3) {
            long distinctColors = combo.stream()
                    .map(c -> c.getColor() == null ? "" : c.getColor().toLowerCase(Locale.ROOT))
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .count();
            if (distinctColors >= 3) return false;
        }

        return true;
    }

    /**
     * 0..1 arası basit kural skoru (istersen DB'ye de yazarsın).
     * Bu skor AI skoru değil; sadece "kural uyumu".
     */
    public double ruleScore(List<Clothe> combo) {
        double score = 0.5;

        // neutral bonus
        boolean hasNeutral = combo.stream().map(Clothe::getColor).anyMatch(RuleBasedOutfitFilter::isNeutral);
        if (hasNeutral) score += 0.15;

        // earth tone bonus (beige + brown)
        boolean hasBeige = combo.stream().anyMatch(c -> "beige".equalsIgnoreCase(c.getColor()));
        boolean hasBrown = combo.stream().anyMatch(c -> "brown".equalsIgnoreCase(c.getColor()));
        if (hasBeige && hasBrown) score += 0.10;

        // patterned penalty
        long patternCount = combo.stream().map(Clothe::getColor).filter(RuleBasedOutfitFilter::isPattern).count();
        if (patternCount == 1) score -= 0.05;
        if (patternCount >= 2) score -= 0.25;

        // clamp
        if (score < 0) score = 0;
        if (score > 1) score = 1;
        return score;
    }
}
