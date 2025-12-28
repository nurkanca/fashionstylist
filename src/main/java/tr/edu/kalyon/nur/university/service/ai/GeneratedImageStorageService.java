package tr.edu.kalyon.nur.university.service.ai;

import org.springframework.stereotype.Service;
import tr.edu.kalyon.nur.university.storage.GeneratedImageRepository;
import tr.edu.kalyon.nur.university.storage.entity.GeneratedImage;

import java.util.Optional;
import java.util.UUID;

@Service
public class GeneratedImageStorageService {

    private final GeneratedImageRepository repository;

    public GeneratedImageStorageService(GeneratedImageRepository repository) {
        this.repository = repository;
    }

    /** Saves PNG bytes into DB and returns generated imageId */
    public String savePng(byte[] pngBytes, Long outfitId) {
        String string = String.valueOf(outfitId);
        GeneratedImage img = new GeneratedImage(string, "image/png", pngBytes);
        repository.save(img);
        return string;
    }

    /** Reads PNG bytes from DB by imageId */
    public byte[] read(String id) {
        GeneratedImage img = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Generated image not found: " + id));
        return img.getData();
    }

    /** Reads PNG bytes from DB by imageId */
    public boolean exist(String id) {
        Optional<GeneratedImage> byId = repository.findById(id);
        return byId.isPresent();
    }

    public String contentType(String id) {
        GeneratedImage img = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Generated image not found: " + id));
        return img.getContentType();
    }
}
