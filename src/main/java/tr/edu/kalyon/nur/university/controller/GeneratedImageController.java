package tr.edu.kalyon.nur.university.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.kalyon.nur.university.service.ai.GeneratedImageStorageService;

@RestController
@RequestMapping("/api/generated-images")
public class GeneratedImageController {

    private final GeneratedImageStorageService storageService;

    public GeneratedImageController(GeneratedImageStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        byte[] bytes = storageService.read(id);
        String contentType = storageService.contentType(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }
}
