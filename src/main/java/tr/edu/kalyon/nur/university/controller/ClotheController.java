package tr.edu.kalyon.nur.university.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tr.edu.kalyon.nur.university.controller.dto.ClotheCandidateDto;
import tr.edu.kalyon.nur.university.controller.dto.ImageBatchRequest;
import tr.edu.kalyon.nur.university.controller.dto.ImageWithIdResponse;
import tr.edu.kalyon.nur.university.service.ClotheService;
import tr.edu.kalyon.nur.university.service.JwtService;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;

import java.util.*;

@RestController
@RequestMapping("/api/clothe")
public class ClotheController {

    private final ClotheService clotheService;
    private final JwtService jwtService;

    public ClotheController(ClotheService clotheService, JwtService jwtService) {
        this.clotheService = clotheService;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addClothe(@RequestPart("file") MultipartFile file,
                          @RequestHeader("Authorization") String authorization) throws Exception {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }

        String token = authorization.substring(7);
        Long userId = jwtService.extractUserId(token);

        clotheService.addClothe(file, userId);
    }

    @PostMapping(value = "/images", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<ImageWithIdResponse> getImagesByIds(@RequestBody ImageBatchRequest req,
                                                    @RequestHeader("Authorization") String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }

        String token = authorization.substring(7);
        Long userId = jwtService.extractUserId(token);

        List<Long> ids = (req.getIds() == null) ? List.of() : req.getIds();
        if (ids.isEmpty()) return List.of();

        List<Clothe> clothes = clotheService.getOwnedClothesByIds(userId, ids);

        Map<Long, Clothe> byId = new HashMap<>();
        for (Clothe c : clothes) byId.put(c.getId(), c);

        List<ImageWithIdResponse> out = new ArrayList<>();
        for (Long id : ids) {
            Clothe c = byId.get(id);
            if (c == null) continue;

            byte[] data = c.getImageData();
            if (data == null || data.length == 0) continue;

            String base64 = Base64.getEncoder().encodeToString(data);

            String ct = (c.getImageContentType() != null) ? c.getImageContentType() : "application/octet-stream";
            String fn = (c.getImageFilename() != null) ? c.getImageFilename() : "image";

            out.add(new ImageWithIdResponse(c.getId(), ct, fn, base64));
        }

        return out;
    }

    @GetMapping("/list")
    public List<ClotheCandidateDto> listMyClothes(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(value = "type", required = false) String type
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }

        String token = authorization.substring(7);
        Long userId = jwtService.extractUserId(token);

        if (type == null || type.isBlank()) {
            return clotheService.listMyClothes(userId);
        }

        String normalized = type.trim().toLowerCase();

        if (!List.of("top", "bottom", "shoes").contains(normalized)) {
            throw new RuntimeException("Invalid type: " + type + " (use: top, bottom, shoes)");
        }

        return clotheService.listMyClothesByType(userId, normalized);
    }

}
