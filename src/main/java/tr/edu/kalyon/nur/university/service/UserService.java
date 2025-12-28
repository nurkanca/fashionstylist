package tr.edu.kalyon.nur.university.service;

import org.springframework.stereotype.Component;
import tr.edu.kalyon.nur.university.model.User;
import tr.edu.kalyon.nur.university.storage.UserRepository;

import java.util.Optional;

@Component
public class UserService {
    private final UserRepository userRepository;
    private UserRepository userRepo;

    public UserService(UserRepository userRepo, UserRepository userRepository) {
        this.userRepo = userRepo;
        this.userRepository = userRepository;
    }

    public void addUser(String name, String gender) {
        User newUser = new User();
        newUser.setName(name);
        newUser.setGender(gender);
        userRepo.save(newUser);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}