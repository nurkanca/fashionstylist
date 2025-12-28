package tr.edu.kalyon.nur.university.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tr.edu.kalyon.nur.university.controller.dto.OutfitCandidateDto;

import java.util.*;

@Slf4j
@Service
public class OpenAiOutfitSelectorService {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    // application.yml’den de okuyabilirsin
    private final String model;
    private String apiKey;

    public OpenAiOutfitSelectorService(WebClient.Builder builder) {
        this.model = "gpt-4";
        this.model = "gpt-5-mini-2025-08-07";
        this.webClient = builder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Sends:
     *  - userText
     *  - candidate outfits (id + short features)
     * to OpenAI, and receives:
     *  - selectedOutfitIds: [ ... ]
     */
    public List<Long> selectOutfitIds(String userText,
                                      List<OutfitCandidateDto> candidates,
                                      int maxResults) {

        if (apiKey == null || apiKey.isBlank()) {
            log.error("OPENAI_API_KEY is missing. Set it as environment variable.");
            return List.of();
        }

        if (userText == null || userText.isBlank()) {
            return List.of();
        }

        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        // avoid insane numbers
        int safeMax = Math.min(Math.max(1, maxResults), 10);

        try {
            // ✅ JSON Schema for strict output: only selectedOutfitIds
            Map<String, Object> toolSchema = new LinkedHashMap<>();
            toolSchema.put("type", "object");

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("selectedOutfitIds", Map.of(
                    "type", "array",
                    "items", Map.of("type", "integer"),
                    "minItems", 1,
                    "maxItems", safeMax
            ));

            toolSchema.put("properties", props);
            toolSchema.put("required", List.of("selectedOutfitIds"));
            toolSchema.put("additionalProperties", false);

            // ✅ System prompt
            String system = """
            You are an outfit selection engine.
            You will receive:
            - A user's request
            - A list of candidate outfits (only these candidates are allowed)
            
            Rules:
            - Select ONLY from the given candidate outfitIds.
            - Respect constraints like "no black", "winter", "office", etc.
            - Return at most %d outfitIds.
            """.formatted(safeMax);

            // ✅ Build payload for Responses API
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);

            // input messages
            List<Map<String, Object>> input = new ArrayList<>();
            input.add(Map.of("role", "system", "content", system));

            String candidatesJson = mapper.writeValueAsString(candidates);
            input.add(Map.of(
                    "role", "user",
                    "content",
                    "User request: " + userText + "\n\nCandidate outfits JSON:\n" + candidatesJson
            ));

            payload.put("input", input);

            // ✅ IMPORTANT: Responses API tools MUST be FLAT (NOT nested under "function": {...})
            Map<String, Object> tool = new LinkedHashMap<>();
            tool.put("type", "function");
            tool.put("name", "select_outfits");
            tool.put("description", "Select the best matching outfit IDs from the candidate list.");
            tool.put("parameters", toolSchema);
            tool.put("strict", true);

            payload.put("tools", List.of(tool));

            // ✅ IMPORTANT: tool_choice MUST be FLAT
            payload.put("tool_choice", Map.of(
                    "type", "function",
                    "name", "select_outfits"
            ));

            // ✅ Call OpenAI
            String raw = webClient.post()
                    .uri("/responses")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (raw == null || raw.isBlank()) return List.of();

            // ✅ Parse selected IDs from response
            List<Long> selected = parseSelectedIdsFromResponses(raw, safeMax);

            // ✅ Safety: Ensure returned ids exist in candidates
            Set<Long> allowedIds = new HashSet<>();
            for (OutfitCandidateDto c : candidates) {
                if (c != null && c.outfitId() != null) {
                    allowedIds.add(c.outfitId());
                }
            }

            List<Long> cleaned = selected.stream()
                    .filter(Objects::nonNull)
                    .filter(allowedIds::contains)
                    .distinct()
                    .limit(safeMax)
                    .toList();

            return cleaned;

        } catch (WebClientResponseException e) {
            // ✅ This will show you WHY 400 happened
            log.error("OpenAI HTTP ERROR: {}", e.getStatusCode());
            log.error("OpenAI BODY: {}", e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("OpenAI selector failed", e);
            return List.of();
        }
    }

    /**
     * Responses API returns something like:
     * {
     *   "output": [
     *     {
     *       "type": "function_call",
     *       "name": "select_outfits",
     *       "arguments": "{\"selectedOutfitIds\":[101,102]}"
     *     }
     *   ]
     * }
     */
    private List<Long> parseSelectedIdsFromResponses(String rawJson, int maxResults) throws Exception {
        JsonNode root = mapper.readTree(rawJson);

        JsonNode output = root.path("output");
        if (!output.isArray()) return List.of();

        for (JsonNode item : output) {
            String type = item.path("type").asText("");

            // Most common: "function_call"
            if (!"function_call".equals(type)) continue;

            String name = item.path("name").asText("");
            if (!"select_outfits".equals(name)) continue;

            String argsText = item.path("arguments").asText(null);
            if (argsText == null || argsText.isBlank()) return List.of();

            JsonNode args = mapper.readTree(argsText);

            JsonNode idsNode = args.path("selectedOutfitIds");
            if (!idsNode.isArray()) return List.of();

            List<Long> result = new ArrayList<>();
            for (JsonNode idNode : idsNode) {
                if (idNode.isNumber()) {
                    result.add(idNode.asLong());
                }
                if (result.size() >= maxResults) break;
            }
            return result;
        }

        return List.of();
    }
}
