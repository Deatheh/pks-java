package petproject.javapks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.exception.EmailAlreadyExistsException;
import petproject.javapks.exception.UserNotFoundException;
import petproject.javapks.mapper.UserMapper;
import petproject.javapks.model.User;
import petproject.javapks.repository.UserRepository;
import petproject.javapks.security.jwt.JwtService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

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

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public UserDto getInfoAboutMe(String accessToken){
        return userMapper.toDto(getUserByEmail(jwtService.extractUsername(accessToken)));
    }
}
