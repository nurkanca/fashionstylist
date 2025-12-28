package tr.edu.kalyon.nur.university.storage.entity;

import jakarta.persistence.*;
import tr.edu.kalyon.nur.university.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Clothe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String category;
    private String color;
    private String style;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "clothe_seasons", joinColumns = @JoinColumn(name = "clothe_id"))
    @Column(name = "season")
    private List<String> seasons = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @Column(name = "image_filename")
    private String imageFilename;

    @Column(name = "image_content_type")
    private String imageContentType;

    @ManyToMany(mappedBy = "clothes")
    private Set<Outfit> outfits = new HashSet<>();

    public Clothe() {}

    // setters
    public void setCategory(String category){ this.category = category; }
    public void setColor(String color){ this.color = color; }
    public void setStyle(String style){ this.style = style; }
    public void setUser(User user){ this.user = user; }
    public void setSeasons(List<String> seasons) {
        this.seasons = (seasons == null) ? new ArrayList<>() : new ArrayList<>(seasons);
    }

    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public void setImageFilename(String imageFilename) { this.imageFilename = imageFilename; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }

    // getters
    public Long getId(){ return id; }
    public String getCategory(){ return category; }
    public String getColor(){ return color; }
    public String getStyle(){ return style; }
    public List<String> getSeasons(){ return seasons; }

    public byte[] getImageData() { return imageData; }
    public String getImageFilename() { return imageFilename; }
    public String getImageContentType() { return imageContentType; }

    public Set<Outfit> getOutfits() { return outfits; }
    public void setOutfits(Set<Outfit> outfits) { this.outfits = outfits; }
}
