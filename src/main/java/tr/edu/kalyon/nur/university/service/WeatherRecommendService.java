package tr.edu.kalyon.nur.university.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tr.edu.kalyon.nur.university.controller.dto.RecommendByWeatherResponse;
import tr.edu.kalyon.nur.university.storage.OutfitRepository;
import tr.edu.kalyon.nur.university.storage.entity.Outfit;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherRecommendService {

    private final OutfitRepository outfitRepository;

    public RecommendByWeatherResponse recommend(Long userId, double temperature) {

        List<String> seasons = mapTemperatureToSeasons(temperature);

        List<Outfit> outfits;

        boolean isColdGroup = seasons.contains("winter") && seasons.contains("fall");

        if (isColdGroup) {
            // ✅ winter+fall => tüm parçalar bu grupta olmalı
            outfits = outfitRepository.findHighScoreOutfitsWhereAllClothesMatchAnySeasonInList(userId, seasons);
        } else {
            // ✅ spring+summer => en az 1 parça bile uysa yeter
            outfits = outfitRepository.findHighScoreOutfitsBySeasons(userId, seasons);
        }

        List<Long> ids = outfits.stream()
                .map(Outfit::getId)
                .toList();

        return new RecommendByWeatherResponse(
                String.join(",", seasons),
                temperature,
                ids
        );
    }


    private List<String> mapTemperatureToSeasons(double temp) {

        if (temp <= 18) {
            // soğuk aralık = winter + fall birlikte
            return List.of("winter", "fall");
        }

        // sıcak aralık = spring + summer birlikte
        return List.of("spring", "summer");
    }

}
