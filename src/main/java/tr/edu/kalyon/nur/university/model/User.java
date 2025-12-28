package tr.edu.kalyon.nur.university.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String gender;

    public void setName(String name){
        this.name=name;

    }
    public void setGender(String gender){
        this.gender=gender;
    }

    public Long getId(){
        return id;
    }

}
