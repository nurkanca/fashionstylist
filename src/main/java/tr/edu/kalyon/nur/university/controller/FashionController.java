package tr.edu.kalyon.nur.university.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tr.edu.kalyon.nur.university.model.ClipResponse;
import tr.edu.kalyon.nur.university.model.CompatibilityScoreResponse;
import tr.edu.kalyon.nur.university.model.ImagePart;
import tr.edu.kalyon.nur.university.service.AiService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fashion")
public class FashionController {

    private final AiService client;

    public FashionController(AiService client) {
        this.client = client;
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClipResponse extract(@RequestPart("file") MultipartFile file) throws Exception {
        MediaType mt = (file.getContentType() != null)
                ? MediaType.valueOf(file.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        ImagePart part = new ImagePart(file.getBytes(), file.getOriginalFilename(), mt);
        return client.extractAttributes(part);
    }

    @PostMapping(value = "/compat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public double compat(@RequestPart("files") List<MultipartFile> files) throws Exception {
        List<ImagePart> parts = files.stream()
                .map(f -> {
                    MediaType mt = (f.getContentType() != null)
                            ? MediaType.valueOf(f.getContentType())
                            : MediaType.APPLICATION_OCTET_STREAM;
                    try {
                        return new ImagePart(f.getBytes(), f.getOriginalFilename(), mt);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        return client.scoreCompatibility(parts);
    }


}
