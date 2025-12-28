package tr.edu.kalyon.nur.university.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.kalyon.nur.university.model.SaveSurveyRequest;
import tr.edu.kalyon.nur.university.service.UserSurveyPreferenceService;
import tr.edu.kalyon.nur.university.storage.entity.UserSurveyPreference;

@RestController
@RequestMapping("/api/preferences")
public class UserSurveyPreferenceController {

    private final UserSurveyPreferenceService service;

    public UserSurveyPreferenceController(UserSurveyPreferenceService service) {
        this.service = service;
    }

    @PostMapping("/survey")
    public ResponseEntity<UserSurveyPreference> saveSurvey(@RequestBody SaveSurveyRequest req) {
        return ResponseEntity.ok(service.saveSurvey(req));
    }
}
