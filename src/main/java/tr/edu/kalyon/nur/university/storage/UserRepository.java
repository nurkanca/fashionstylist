package tr.edu.kalyon.nur.university.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.edu.kalyon.nur.university.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
