package tr.edu.kalyon.nur.university.storage;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.edu.kalyon.nur.university.storage.entity.Clothe;
import tr.edu.kalyon.nur.university.model.User;

import java.util.List;

public interface ClotheRepository extends JpaRepository<Clothe, Long> {

    List<Clothe> findByUserAndCategory(User user, String category);

    List<Clothe> findByUserId(Long id);

    List<Clothe> findByUserIdAndIdIn(Long userId, List<Long> ids);

    List<Clothe> findByUserIdAndCategoryIgnoreCase(Long userId, String type);
}

