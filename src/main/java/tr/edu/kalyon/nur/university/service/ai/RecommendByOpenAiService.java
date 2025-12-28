package tr.edu.kalyon.nur.university.service.ai;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tr.edu.kalyon.nur.university.controller.dto.ClotheCandidateDto;
import tr.edu.kalyon.nur.university.controller.dto.OutfitCandidateDto;
import tr.edu.kalyon.nur.university.controller.dto.RecommendByOpenAiResponse;
import tr.edu.kalyon.nur.university.storage.OutfitRepository;
import tr.edu.kalyon.nur.university.storage.entity.Outfit;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendByOpenAiService {

    private final OutfitRepository outfitRepository;
    private final OpenAiOutfitSelectorService openAiSelector;

    public RecommendByOpenAiService(OutfitRepository outfitRepository,
                                    OpenAiOutfitSelectorService openAiSelector) {
        this.outfitRepository = outfitRepository;
        this.openAiSelector = openAiSelector;
    }

    public RecommendByOpenAiResponse recommend(Long userId, String userText) {
        if (userId == null || userText == null || userText.isBlank()) {
            return new RecommendByOpenAiResponse(
                    "Lütfen ne istediğini yaz (ör: ofis, kışlık, siyah olmasın).",
                    List.of(),
                    List.of()
            );
        }

        List<Outfit> topOutfits = outfitRepository.findTopCandidates(userId);

        if (topOutfits.isEmpty()) {
            return new RecommendByOpenAiResponse(
                    "Dolabında henüz yeterli kombin yok. Önce birkaç kıyafet ekle, sonra öneri yapayım.",
                    List.of(),
                    List.of()
            );
        }

        // 2) OpenAI'a göndereceğimiz candidate list
        List<OutfitCandidateDto> candidates = topOutfits.stream()
                .map(this::toCandidateDto)
                .toList();


        // 3) OpenAI’dan seçili outfit ID’leri al
        List<Long> selected = openAiSelector.selectOutfitIds(userText, candidates, 5);
        // 4) Güvenlik: OpenAI sadece bizim candidate id’lerinden seçsin
        Set<Long> allowed = candidates.stream().map(OutfitCandidateDto::outfitId).collect(Collectors.toSet());
        List<Long> cleanSelected = selected.stream()
                .filter(Objects::nonNull)
                .filter(allowed::contains)
                .distinct()
                .limit(5)
                .toList();

        // 5) Eğer hiçbir şey dönmediyse fallback: score’a göre top 5
        if (cleanSelected.isEmpty()) {
            List<OutfitCandidateDto> fallback = candidates.stream().limit(5).toList();
            return new RecommendByOpenAiResponse(
                    "Kriterleri tam yakalayamadım ama en uyumlu kombinler şunlar:",
                    fallback.stream().map(OutfitCandidateDto::outfitId).toList(),
                    fallback
            );
        }

        // 6) Seçilen outfitleri DB’den getir (detaylı)
        // Not: candidates zaten outfit bilgilerini taşıyor, istersen DB’ye tekrar gitmeden dönebilirsin.
        // Ben burada candidates üzerinden response dönüyorum (performans iyi).
        Map<Long, OutfitCandidateDto> map = candidates.stream()
                .collect(Collectors.toMap(OutfitCandidateDto::outfitId, x -> x, (a, b) -> a));

        List<OutfitCandidateDto> selectedDtos = cleanSelected.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .toList();

        return new RecommendByOpenAiResponse(
                "İsteğine göre en iyi kombinler:",
                cleanSelected,
                selectedDtos
        );
    }

    private OutfitCandidateDto toCandidateDto(Outfit o) {
        List<ClotheCandidateDto> clothes = o.getClothes().stream()
                .map(c -> new ClotheCandidateDto(
                        c.getId(),
                        safeLower(c.getCategory()),
                        safeLower(c.getColor()),
                        safeLower(c.getStyle()),
                        c.getSeasons() == null ? List.of() : c.getSeasons()
                ))
                .toList();

        return new OutfitCandidateDto(o.getId(), o.getScore(), clothes);
    }

    private String safeLower(String s) {
        return s == null ? null : s.toLowerCase(Locale.ROOT).trim();
    }
}
