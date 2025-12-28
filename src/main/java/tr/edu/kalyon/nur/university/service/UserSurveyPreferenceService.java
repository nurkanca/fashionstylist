package tr.edu.kalyon.nur.university.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.kalyon.nur.university.model.SaveSurveyRequest;
import tr.edu.kalyon.nur.university.storage.UserSurveyPreferenceRepository;
import tr.edu.kalyon.nur.university.storage.entity.UserSurveyPreference;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class UserSurveyPreferenceService {

    private final UserSurveyPreferenceRepository repo;

    private static final Set<String> ALLOWED_STYLES =
            Set.of("casual", "formal", "street", "sporty", "minimal");

    public UserSurveyPreferenceService(UserSurveyPreferenceRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public UserSurveyPreference saveSurvey(SaveSurveyRequest req, Long userId) {

        UserSurveyPreference pref = repo.findById(userId)
                .orElseGet(() -> new UserSurveyPreference(userId));

        pref.setFavoriteColors(normalizeSet(req.favoriteColors()));
        pref.setFavoriteStyles(filterAllowedStyles(normalizeSet(req.favoriteStyles())));
        pref.setOutfitPreference(parseOutfitPref(req.outfitPreference()));

        return repo.save(pref);
    }

    public UserSurveyPreference getOrDefault(Long userId) {
        return repo.findById(userId).orElseGet(() -> new UserSurveyPreference(userId));
    }

    private Set<String> normalizeSet(Set<String> in) {
        if (in == null) return new HashSet<>();
        Set<String> out = new HashSet<>();
        for (String s : in) {
            if (s == null) continue;
            String v = s.trim().toLowerCase(Locale.ROOT);
            if (!v.isBlank()) out.add(v);
        }
        return out;
    }

    private Set<String> filterAllowedStyles(Set<String> styles) {
        Set<String> out = new HashSet<>();
        for (String s : styles) {
            if (ALLOWED_STYLES.contains(s)) out.add(s);
        }
        return out;
    }

    private UserSurveyPreference.OutfitPreference parseOutfitPref(String s) {
        if (s == null || s.isBlank()) return UserSurveyPreference.OutfitPreference.EQUAL;
        try {
            return UserSurveyPreference.OutfitPreference.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return UserSurveyPreference.OutfitPreference.EQUAL;
        }
    }

    public UserSurveyPreference getByUserId(Long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Preferences not found for userId=" + userId));
    }
}
