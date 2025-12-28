package tr.edu.kalyon.nur.university.storage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "generated_images")
@Getter
@Setter
@NoArgsConstructor
public class GeneratedImage {

    @Id
    @Column(length = 36)
    private String id; // UUID string

    @Column(nullable = false)
    private String contentType; // "image/png"

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public GeneratedImage(String id, String contentType, byte[] data) {
        this.id = id;
        this.contentType = contentType;
        this.data = data;
        this.createdAt = Instant.now();
    }
}
