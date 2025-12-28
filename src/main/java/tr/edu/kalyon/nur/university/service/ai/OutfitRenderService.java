package tr.edu.kalyon.nur.university.service.ai;

import org.springframework.stereotype.Service;
import tr.edu.kalyon.nur.university.storage.OutfitRepository;
import tr.edu.kalyon.nur.university.storage.entity.Outfit;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;

import java.util.List;

@Service
public class OutfitRenderService {

    private final OutfitRepository outfitRepository;
    private final OpenAiImageService openAiImageService;
    private final GeneratedImageStorageService storageService;

    public OutfitRenderService(
            OutfitRepository outfitRepository,
            OpenAiImageService openAiImageService,
            GeneratedImageStorageService storageService
    ) {
        this.outfitRepository = outfitRepository;
        this.openAiImageService = openAiImageService;
        this.storageService = storageService;
    }

    public String renderOnModel(Long outfitId) {

        boolean exist = storageService.exist(String.valueOf(outfitId));
        if (exist) {
            return String.valueOf(outfitId);
        }

        Outfit outfit = outfitRepository.findById(outfitId)
                .orElseThrow(() -> new RuntimeException("Outfit not found"));

        List<byte[]> images = outfit.getClothes().stream()
                .map(Clothe::getImageData)
                .toList();

        String prompt = "Combine these clothing items into a single realistic outfit on a human model, "
                + "front view, clean background, high quality fashion photo.";

        byte[] pngBytes = openAiImageService.renderOutfitFromImages(images, prompt);

        // ✅ save & return URL
        return storageService.savePng(pngBytes, outfitId);
    }
}
