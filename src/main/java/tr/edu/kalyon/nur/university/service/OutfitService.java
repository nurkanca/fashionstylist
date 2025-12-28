package tr.edu.kalyon.nur.university.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;
import tr.edu.kalyon.nur.university.model.ImagePart;
import tr.edu.kalyon.nur.university.storage.entity.Outfit;
import tr.edu.kalyon.nur.university.model.User;
import tr.edu.kalyon.nur.university.storage.ClotheRepository;
import tr.edu.kalyon.nur.university.storage.OutfitRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

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

    private void saveOutfitWithScore(User user, List<Clothe> combo) {
        double score;
        double personal;
        try {
            List<ImagePart> parts = new ArrayList<>(combo.size());
            for (Clothe c : combo) parts.add(toImagePart(c));
            score = aiService.scoreCompatibility(parts);
            personal = personalScoreService.score(user.getId(), combo);
        } catch (Exception e) {
            return;
        }

        Outfit outfit = new Outfit();
        outfit.setUser(user);
        outfit.setScore(score);
        outfit.setPersonalScore(personal);
        outfit.setClothes(new HashSet<>(combo));
        outfitRepository.save(outfit);
    }

    private ImagePart toImagePart(Clothe c) {
        MediaType mt = (c.getImageContentType() != null)
                ? MediaType.valueOf(c.getImageContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        String filename = (c.getImageFilename() != null) ? c.getImageFilename() : "image.jpg";
        return new ImagePart(c.getImageData(), filename, mt);
    }

}
