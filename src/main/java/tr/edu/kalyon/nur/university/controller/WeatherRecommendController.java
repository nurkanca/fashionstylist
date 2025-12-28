package tr.edu.kalyon.nur.university.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tr.edu.kalyon.nur.university.controller.dto.RecommendByWeatherRequest;
import tr.edu.kalyon.nur.university.controller.dto.RecommendByWeatherResponse;
import tr.edu.kalyon.nur.university.service.JwtService;
import tr.edu.kalyon.nur.university.service.WeatherRecommendService;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class WeatherRecommendController {

    private final JwtService jwtService;
    private final WeatherRecommendService weatherRecommendService;

    @PostMapping("/by-weather")
    public RecommendByWeatherResponse recommendByWeather(
            @RequestBody RecommendByWeatherRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        String token = authorization.substring(7);
        Long userId = jwtService.extractUserId(token);

        return weatherRecommendService.recommend(userId, request.temperature());
    }
}
