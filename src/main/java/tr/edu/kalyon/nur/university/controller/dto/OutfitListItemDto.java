package tr.edu.kalyon.nur.university.controller.dto;

import java.util.List;

public class OutfitListItemDto {
    private Long id;
    private Double score;
    private Double personalScore;
    private List<Long> clotheIds;

    public OutfitListItemDto(Long id, Double score, Double personalScore, List<Long> clotheIds) {
        this.id = id;
        this.score = score;
        this.personalScore = personalScore;
        this.clotheIds = clotheIds;
    }

    public Long getId() { return id; }
    public Double getScore() { return score; }
    public Double getPersonalScore() { return personalScore; }
    public List<Long> getClotheIds() { return clotheIds; }
}
