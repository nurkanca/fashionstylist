package tr.edu.kalyon.nur.university.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;   // or email

    @Column(nullable = false)
    private String password;   // hashed

    private String name;
    private String gender;

}
