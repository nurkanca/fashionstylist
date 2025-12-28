package tr.edu.kalyon.nur.university.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.kalyon.nur.university.controller.dto.ClotheDetailDto;
import tr.edu.kalyon.nur.university.controller.dto.OutfitDetailResponse;
import tr.edu.kalyon.nur.university.service.ai.AiService;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;
import tr.edu.kalyon.nur.university.model.ImagePart;
import tr.edu.kalyon.nur.university.storage.entity.Outfit;
import tr.edu.kalyon.nur.university.model.User;
import tr.edu.kalyon.nur.university.storage.ClotheRepository;
import tr.edu.kalyon.nur.university.storage.OutfitRepository;

import java.util.*;

@Service
public class OutfitService {

    private final OutfitRepository outfitRepository;
    private final ClotheRepository clotheRepository;
    private final AiService aiService;
    private final PersonalScoreService personalScoreService;

    public OutfitService(OutfitRepository outfitRepository, ClotheRepository clotheRepository, AiService aiService, PersonalScoreService personalScoreService) {
        this.outfitRepository = outfitRepository;
        this.clotheRepository = clotheRepository;
        this.aiService = aiService;
        this.personalScoreService = personalScoreService;
    }

    @Transactional(readOnly = true)
    public List<Outfit> listByScoreDesc(Long userId) {
        return outfitRepository.findByUserIdOrderByScoreDesc(userId);
    }


    @Transactional
    public void createPossibleOutfit(User user, Clothe seed) {
        List<Clothe> wardrobe = clotheRepository.findByUserId(user.getId());
        if (wardrobe == null || wardrobe.isEmpty()) return;

        wardrobe = wardrobe.stream()
                .filter(c -> c.getImageData() != null && c.getImageData().length > 0)
                .toList();

        if (seed.getImageData() == null || seed.getImageData().length == 0) return;

        String seedCat = seed.getCategory() == null ? "" : seed.getCategory().toLowerCase(Locale.ROOT);

        List<Clothe> tops      = byCategory(wardrobe, "top", seed);
        List<Clothe> bottoms   = byCategory(wardrobe, "bottom", seed);
        List<Clothe> shoes     = byCategory(wardrobe, "shoes", seed);
        List<Clothe> onePieces = byCategory(wardrobe, "one_piece", seed);

        int savedCount = 0;
        final int FLUSH_EVERY = 200; // not a cap; just memory protection

        if (seedCat.equals("top")) {
            for (Clothe b : bottoms) {
                for (Clothe s : shoes) {
                    saveOutfitWithScore(user, List.of(seed, b, s));
                    savedCount++;
                    if (savedCount % FLUSH_EVERY == 0) outfitRepository.flush();
                }
            }
            return;
        }

        if (seedCat.equals("bottom")) {
            // top + bottom + shoes (seed is bottom)
            for (Clothe t : tops) {
                for (Clothe s : shoes) {
                    saveOutfitWithScore(user, List.of(t, seed, s));
                    savedCount++;
                    if (savedCount % FLUSH_EVERY == 0) outfitRepository.flush();
                }
            }
            return;
        }

        if (seedCat.equals("one_piece")) {
            for (Clothe s : shoes) {
                saveOutfitWithScore(user, List.of(seed, s));
                savedCount++;
                if (savedCount % FLUSH_EVERY == 0) outfitRepository.flush();
            }
            return;
        }

        if (seedCat.equals("shoes")) {
            for (Clothe t : tops) {
                for (Clothe b : bottoms) {
                    saveOutfitWithScore(user, List.of(t, b, seed));
                    savedCount++;
                    if (savedCount % FLUSH_EVERY == 0) outfitRepository.flush();
                }
            }

            for (Clothe op : onePieces) {
                saveOutfitWithScore(user, List.of(op, seed));
                savedCount++;
                if (savedCount % FLUSH_EVERY == 0) outfitRepository.flush();
            }
        }
    }

    private List<Clothe> byCategory(List<Clothe> wardrobe, String category, Clothe seed) {
        return wardrobe.stream()
                .filter(c -> c.getId() != null && !c.getId().equals(seed.getId()))
                .filter(c -> category.equalsIgnoreCase(c.getCategory()))
                .toList();
    }

    public OutfitDetailResponse getOutfitDetail(Long userId, Long outfitId) {

        Outfit outfit = outfitRepository.findByIdAndUserIdFetchClothes(outfitId, userId)
                .orElseThrow(() -> new RuntimeException("Outfit not found"));

        List<ClotheDetailDto> clothes = outfit.getClothes().stream()
                .map(c -> new ClotheDetailDto(
                        c.getId(),
                        c.getCategory(),
                        c.getColor(),
                        c.getStyle(),
                        c.getImageContentType(),
                        c.getImageData() != null ? Base64.getEncoder().encodeToString(c.getImageData()) : null
                ))
                .toList();

        return new OutfitDetailResponse(
                outfit.getId(),
                outfit.getScore(),
                outfit.getPersonalScore(),
                clothes
        );
    }

    private void saveOutfitWithScore(User user, List<Clothe> combo) {
        double aiScore;
        double personalScore;
        double finalScore;

        try {
            List<ImagePart> parts = new ArrayList<>(combo.size());
            for (Clothe c : combo) parts.add(toImagePart(c));

            personalScore = personalScoreService.score(user.getId(), combo); // 0..1 gibi varsayalım
            aiScore = aiService.scoreCompatibility(parts);                  // 0..1 gibi varsayalım

            // ✅ Personal score küçük etki etsin (%10)
            double alpha = 0.10; // personal etkisi
            finalScore = (1 - alpha) * aiScore + alpha * personalScore;

        } catch (Exception e) {
            return;
        }

        Outfit outfit = new Outfit();
        outfit.setUser(user);

        // ✅ hem AI score hem personalScore sakla
        outfit.setScore(round2(finalScore));
        outfit.setPersonalScore(round2(personalScore));

        outfit.setClothes(new HashSet<>(combo));
        outfitRepository.save(outfit);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private ImagePart toImagePart(Clothe c) {
        MediaType mt = (c.getImageContentType() != null)
                ? MediaType.valueOf(c.getImageContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        String filename = (c.getImageFilename() != null) ? c.getImageFilename() : "image.jpg";
        return new ImagePart(c.getImageData(), filename, mt);
    }

    public Outfit rateOutfit(Long userId, Long outfitId, Double newPersonalScore) {
        if (newPersonalScore == null) {
            throw new RuntimeException("personalScore is required");
        }

        // ✅ Normalize allowed input:
        // if user sends 1..10 -> convert to 0..1
        // if user sends 0..100 -> convert to 0..1
        double ps = newPersonalScore;
        if (ps > 1.0 && ps <= 10.0) ps = ps / 10.0;
        if (ps > 10.0 && ps <= 100.0) ps = ps / 100.0;

        if (ps < 0.0 || ps > 1.0) {
            throw new RuntimeException("personalScore must be between 0.0 and 1.0 (or 1..10 or 0..100)");
        }

        Outfit outfit = outfitRepository.findByIdAndUserId(outfitId, userId)
                .orElseThrow(() -> new RuntimeException("Outfit not found or not owned by user"));

        // ✅ overwrite personalScore
        outfit.setPersonalScore(round2(ps));

        // ✅ update final score by 10%
        double oldFinalScore = outfit.getScore() == null ? 0.0 : outfit.getScore();
        double newFinalScore = (oldFinalScore * 0.90) + (ps * 0.10);

        outfit.setScore(round2(newFinalScore));

        return outfitRepository.save(outfit);
    }


}
