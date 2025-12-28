package tr.edu.kalyon.nur.university.storage.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_survey_preferences")
public class UserSurveyPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_favorite_colors", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "color")
    private Set<String> favoriteColors = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_favorite_styles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "style")
    private Set<String> favoriteStyles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "outfit_preference", nullable = false)
    private OutfitPreference outfitPreference = OutfitPreference.EQUAL;

    public enum OutfitPreference {
        ONE_PIECE,
        TOP_BOTTOM,
        EQUAL
    }

    public UserSurveyPreference() {}

    public UserSurveyPreference(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() { return userId; }

    public Set<String> getFavoriteColors() { return favoriteColors; }
    public void setFavoriteColors(Set<String> favoriteColors) {
        this.favoriteColors = (favoriteColors == null) ? new HashSet<>() : favoriteColors;
    }

    public Set<String> getFavoriteStyles() { return favoriteStyles; }
    public void setFavoriteStyles(Set<String> favoriteStyles) {
        this.favoriteStyles = (favoriteStyles == null) ? new HashSet<>() : favoriteStyles;
    }

    public OutfitPreference getOutfitPreference() { return outfitPreference; }
    public void setOutfitPreference(OutfitPreference outfitPreference) {
        this.outfitPreference = (outfitPreference == null) ? OutfitPreference.EQUAL : outfitPreference;
    }
}
