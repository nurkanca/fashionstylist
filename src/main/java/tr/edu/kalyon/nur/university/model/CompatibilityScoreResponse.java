package tr.edu.kalyon.nur.university.model;

public record CompatibilityScoreResponse(
        double probability,
        double logit,
        int images_found,
        int items_requested
) {}
