package tr.edu.kalyon.nur.university.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.edu.kalyon.nur.university.storage.entity.Outfit;

   public interface OutfitRepository extends JpaRepository<Outfit , Long> {
    }
