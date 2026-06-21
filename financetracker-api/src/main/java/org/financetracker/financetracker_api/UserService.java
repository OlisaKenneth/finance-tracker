package org.financetracker.financetracker_api;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    public User register(String name, String email, String password, String role){
        // check if email already exists before creating anything
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        String hash = passwordEncoder.encode(password);
        newUser.setPassword(hash);
        newUser.setRole(role);

        return userRepository.save(newUser);
    }



}
