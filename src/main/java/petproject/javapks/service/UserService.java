package petproject.javapks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import petproject.javapks.exception.EmailAlreadyExistsException;
import petproject.javapks.exception.UserNotFoundException;
import petproject.javapks.model.User;
import petproject.javapks.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found by email"));
    }

    public User getUserByUUID(UUID uuid){
        return userRepository.findByUuid(uuid).orElseThrow(() -> new UserNotFoundException("User not found by uuid"));
    }

    public Page<User> getUserByParams(Specification<User> spec, PageRequest pageRequest){
        return userRepository.findAll(spec, pageRequest);
    }

    public User updateUser(User user){
        return userRepository.save(user);
    }

    public void deleteUser(User user){
        userRepository.delete(user);
    }
}
