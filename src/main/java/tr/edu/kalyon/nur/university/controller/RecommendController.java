package tr.edu.kalyon.nur.university.controller;

import org.springframework.web.bind.annotation.*;
import tr.edu.kalyon.nur.university.controller.dto.RecommendByOpenAiResponse;
import tr.edu.kalyon.nur.university.controller.dto.RecommendChatRequest;
import tr.edu.kalyon.nur.university.service.JwtService;
import tr.edu.kalyon.nur.university.service.ai.RecommendByOpenAiService;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendByOpenAiService recommendByOpenAiService;
    private final JwtService jwtService;

    public RecommendController(RecommendByOpenAiService recommendByOpenAiService, JwtService jwtService) {
        this.recommendByOpenAiService = recommendByOpenAiService;
        this.jwtService = jwtService;
    }

    @PostMapping("/by-chat")
    public RecommendByOpenAiResponse recommend(@RequestBody RecommendChatRequest request, @RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        Long userId = jwtService.extractUserId(token);
        return recommendByOpenAiService.recommend(userId, request.text());
    }
}
