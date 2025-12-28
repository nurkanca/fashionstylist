package tr.edu.kalyon.nur.university.controller;

import org.springframework.web.bind.annotation.*;
import tr.edu.kalyon.nur.university.controller.dto.OutfitDetailResponse;
import tr.edu.kalyon.nur.university.controller.dto.OutfitListItemDto;
import tr.edu.kalyon.nur.university.service.JwtService;
import tr.edu.kalyon.nur.university.service.OutfitService;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;

import java.util.List;

@RestController
@RequestMapping("/api/outfit")

public class OutfitController {
    private final OutfitService outfitService;
    private final JwtService jwtService;

    public OutfitController(OutfitService outfitService, JwtService jwtService){
        this.outfitService=outfitService;
        this.jwtService = jwtService;
    }

    @GetMapping("/list")
    public List<OutfitListItemDto> list(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        Long userId = jwtService.extractUserId(token);

        return outfitService.listByScoreDesc(userId).stream()
                .map(o -> new OutfitListItemDto(
                        o.getId(),
                        o.getScore(),
                        o.getPersonalScore(),
                        o.getClothes().stream()
                                .map(Clothe::getId)
                                .toList()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public OutfitDetailResponse getOutfitDetail(
            @PathVariable("id") Long outfitId,
            @RequestHeader("Authorization") String authorization
    ) {
        String token = authorization.substring(7);
        Long userId = jwtService.extractUserId(token);

        return outfitService.getOutfitDetail(userId, outfitId);
    }


}
