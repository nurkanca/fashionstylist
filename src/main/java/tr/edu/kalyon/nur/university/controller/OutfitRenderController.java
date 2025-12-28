package tr.edu.kalyon.nur.university.controller;

import org.springframework.web.bind.annotation.*;
import tr.edu.kalyon.nur.university.service.ai.OutfitRenderService;

import java.util.Map;

@RestController
@RequestMapping("/api/outfits")
public class OutfitRenderController {

    private final OutfitRenderService outfitRenderService;

    public OutfitRenderController(OutfitRenderService outfitRenderService) {
        this.outfitRenderService = outfitRenderService;
    }

    @PostMapping("/{outfitId}/render")
    public Map<String, String> render(@PathVariable Long outfitId) {
        String imageUrl = outfitRenderService.renderOnModel(outfitId);
        return Map.of("imageUrl", imageUrl);
    }
}
