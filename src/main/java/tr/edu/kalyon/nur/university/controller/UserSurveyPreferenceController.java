package tr.edu.kalyon.nur.university.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.kalyon.nur.university.model.SaveSurveyRequest;
import tr.edu.kalyon.nur.university.service.JwtService;
import tr.edu.kalyon.nur.university.service.UserSurveyPreferenceService;
import tr.edu.kalyon.nur.university.storage.entity.UserSurveyPreference;

@RestController
@RequestMapping("/api/preferences")
public class UserSurveyPreferenceController {

    private final UserSurveyPreferenceService service;
    private final JwtService jwtService;

    public UserSurveyPreferenceController(UserSurveyPreferenceService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping("/survey")
    public ResponseEntity<UserSurveyPreference> saveSurvey(
            @RequestBody SaveSurveyRequest req,
            @RequestHeader("Authorization") String authorization
    ) {
        // ✅ token kontrol
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }

        String token = authorization.substring(7);
        Long userId = jwtService.extractUserId(token);

        // ✅ client'tan userId almak yerine token'dan alıyoruz (daha güvenli)
        SaveSurveyRequest safeReq = new SaveSurveyRequest(
                req.favoriteColors(),
                req.favoriteStyles(),
                req.outfitPreference()
        );

        return ResponseEntity.ok(service.saveSurvey(safeReq, userId));
    }

    // ✅ NEW: load current user's preferences
    @GetMapping("/me")
    public ResponseEntity<UserSurveyPreference> getMyPreferences(
            @RequestHeader("Authorization") String authorization
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }

        String token = authorization.substring(7);
        Long userId = jwtService.extractUserId(token);

        return ResponseEntity.ok(service.getByUserId(userId));
    }
}
