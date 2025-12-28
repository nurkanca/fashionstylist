package tr.edu.kalyon.nur.university.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class OpenAiImageService {

    private final String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiImageService() {

        // ✅ FIX: OpenAI image response base64 çok büyük olabiliyor (256KB default yetmiyor)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(config -> config.defaultCodecs().maxInMemorySize(20 * 1024 * 1024)) // 20MB
                .build();

        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .build();

        this.apiKey = "";
    }

    /**
     * OpenAI image edit endpointinden PNG bytes döner.
     */
    public byte[] renderOutfitFromImages(List<byte[]> clothesImages, String prompt) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // ✅ model (gpt-image-1 verification sıkıntısı varsa 1.5 kullan)
            body.add("model", "gpt-image-1.5");
            body.add("prompt", prompt);

            // ✅ çoklu image için duplicate param hatasını çöz:
            // OpenAI hata mesajı: image yerine image[] kullan
            for (int i = 0; i < clothesImages.size(); i++) {
                final int index = i;
                byte[] imageBytes = clothesImages.get(i);

                ByteArrayResource resource = new ByteArrayResource(imageBytes) {
                    @Override
                    public String getFilename() {
                        return "clothe_" + index + ".png";  // Use the final copy
                    }
                };

                body.add("image[]", resource);
            }

            String response = webClient.post()
                    .uri("/images/edits")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                throw new RuntimeException("OpenAI image response body boş geldi");
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode data0 = root.path("data").get(0);
            String b64 = data0.path("b64_json").asText(null);

            if (b64 == null || b64.isBlank()) {
                throw new RuntimeException("OpenAI response içinde b64_json yok");
            }

            return Base64.getDecoder().decode(b64);

        } catch (WebClientResponseException e) {
            log.error("OpenAI Image HTTP ERROR: {} {}", e.getStatusCode(), e.getStatusText());
            log.error("OpenAI Image BODY: {}", e.getResponseBodyAsString());
            throw new RuntimeException("OpenAI image error: " + e.getResponseBodyAsString(), e);

        } catch (Exception e) {
            throw new RuntimeException("OpenAI image parse/save error: " + e.getMessage(), e);
        }
    }
}
