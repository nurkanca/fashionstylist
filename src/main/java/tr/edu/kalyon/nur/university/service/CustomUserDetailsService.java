package tr.edu.kalyon.nur.university.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import tr.edu.kalyon.nur.university.model.User;
import tr.edu.kalyon.nur.university.storage.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // No roles for now
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword())
                .authorities("USER")
                .build();
    }
}
