package tr.edu.kalyon.nur.university.controller;

import org.springframework.web.bind.annotation.*;
import tr.edu.kalyon.nur.university.controller.dto.RateOutfitRequest;
import tr.edu.kalyon.nur.university.service.JwtService;
import tr.edu.kalyon.nur.university.service.OutfitService;
import tr.edu.kalyon.nur.university.storage.entity.Outfit;

import java.util.Map;

@RestController
@RequestMapping("/api/outfit")
public class OutfitRatingController {

    private final OutfitService outfitService;
    private final JwtService jwtService;

    public OutfitRatingController(OutfitService outfitService, JwtService jwtService) {
        this.outfitService = outfitService;
        this.jwtService = jwtService;
    }

    @PostMapping("/{outfitId}/rate")
    public Map<String, Object> rateOutfit(@PathVariable Long outfitId,
                                          @RequestBody RateOutfitRequest req,
                                          @RequestHeader("Authorization") String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }

        String token = authorization.substring(7);
        Long userId = jwtService.extractUserId(token);

        Outfit updated = outfitService.rateOutfit(userId, outfitId, req.personalScore());

        return Map.of(
                "outfitId", updated.getId(),
                "score", updated.getScore(),
                "personalScore", updated.getPersonalScore()
        );
    }
}
