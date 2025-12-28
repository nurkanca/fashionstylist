package tr.edu.kalyon.nur.university.storage.entity;

import jakarta.persistence.*;
import tr.edu.kalyon.nur.university.model.User;

import java.util.Set;
import java.util.HashSet;

@Entity
public class Outfit {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private Double score;
    private Double personalScore;      // 0..1

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "outfit_clothes",
            joinColumns = @JoinColumn(name = "outfit_id"),
            inverseJoinColumns = @JoinColumn(name = "clothe_id")
    )
    private Set<Clothe> clothes = new HashSet<>();


    public void setScore(Double score){
        this.score=score;
    }

    public void setUser(User user){
        this.user=user;
    }

    public Double getScore(){
        return score;
    }

    public User getUser(){
        return user;
    }

    public Long getId(){
        return id ;
    }


    public void setClothes(Set<Clothe> clothes) {
        this.clothes = clothes;
    }

    public Double getPersonalScore() {
        return personalScore;
    }

    public void setPersonalScore(Double personalScore) {
        this.personalScore = personalScore;
    }
}
