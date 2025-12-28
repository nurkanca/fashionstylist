package tr.edu.kalyon.nur.university.service.ai;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tr.edu.kalyon.nur.university.model.ClipResponse;
import tr.edu.kalyon.nur.university.model.CompatibilityScoreResponse;
import tr.edu.kalyon.nur.university.model.ImagePart;

import java.util.List;

@Service
public class AiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8000";

    public ClipResponse extractAttributes(ImagePart img) {

        ByteArrayResource resource = new ByteArrayResource(img.bytes()) {
            @Override
            public String getFilename() {
                return (img.filename() == null || img.filename().isBlank()) ? "image.jpg" : img.filename();
            }
        };

        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(img.mediaType() != null ? img.mediaType() : MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<ByteArrayResource> partEntity = new HttpEntity<>(resource, partHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", partEntity); // must be "file" for FastAPI /extract

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<ClipResponse> response = restTemplate.exchange(
                baseUrl + "/extract",
                HttpMethod.POST,
                requestEntity,
                ClipResponse.class
        );

        ClipResponse resp = response.getBody();
        if (resp == null) {
            throw new RuntimeException("Empty response from AI /extract");
        }
        return resp;
    }

    public double scoreCompatibility(List<ImagePart> images) {
        if (images == null || images.size() < 2) {
            throw new IllegalArgumentException("Provide at least 2 images");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (ImagePart img : images) {
            ByteArrayResource resource = new ByteArrayResource(img.bytes()) {
                @Override public String getFilename() {
                    return (img.filename() == null || img.filename().isBlank()) ? "image.jpg" : img.filename();
                }
            };

            HttpHeaders partHeaders = new HttpHeaders();
            partHeaders.setContentType(img.mediaType() != null ? img.mediaType() : MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<ByteArrayResource> partEntity = new HttpEntity<>(resource, partHeaders);

            body.add("files", partEntity);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<CompatibilityScoreResponse> response = restTemplate.exchange(
                baseUrl + "/compat/score-images",
                HttpMethod.POST,
                request,
                CompatibilityScoreResponse.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("Empty response from AI");
        }
        return response.getBody().probability();
    }

    // helper record for input
}
