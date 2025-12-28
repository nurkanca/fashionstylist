package tr.edu.kalyon.nur.university.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.edu.kalyon.nur.university.model.User;

public interface UserRepository extends JpaRepository <User, Long> {

}
