package tr.edu.kalyon.nur.university.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.edu.kalyon.nur.university.storage.entity.UserSurveyPreference;

public interface UserSurveyPreferenceRepository extends JpaRepository<UserSurveyPreference, Long> {
}
