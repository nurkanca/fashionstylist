package tr.edu.kalyon.nur.university.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.edu.kalyon.nur.university.storage.entity.GeneratedImage;

public interface GeneratedImageRepository extends JpaRepository<GeneratedImage, String> {
}
