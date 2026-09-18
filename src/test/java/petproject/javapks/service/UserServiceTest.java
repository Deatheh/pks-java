package petproject.javapks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import petproject.javapks.exception.EmailAlreadyExistsException;
import petproject.javapks.exception.UserNotFoundException;
import petproject.javapks.model.Role;
import petproject.javapks.model.User;
import petproject.javapks.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String RAW_PASSWORD = "rawPassword";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPasswordHash";

    private User buildUser() {
        User user = new User();
        user.setUuid(USER_UUID);
        user.setEmail("test@example.com");
        user.setPassword(RAW_PASSWORD);
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setFirstName("Johnny");
        user.setLastName("Doeman");
        user.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));
        user.setUpdatedAt(LocalDateTime.of(2024, 1, 2, 12, 0));
        return user;
    }

    // ===================== createUser =====================

    @Test
    void createUser_shouldEncodePasswordAndSave_whenEmailIsFree() {
        // given
        User user = buildUser();
        User saved = buildUser();
        saved.setPassword(ENCODED_PASSWORD);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        // when
        User result = userService.createUser(user);

        // then
        assertThat(result).isSameAs(saved);
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode(RAW_PASSWORD);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_shouldThrow_whenEmailAlreadyExists() {
        // given
        User user = buildUser();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already in use");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // ===================== getUserByEmail =====================

    @Test
    void getUserByEmail_shouldReturnUser_whenExists() {
        // given
        User user = buildUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // when
        User result = userService.getUserByEmail("test@example.com");

        // then
        assertThat(result).isSameAs(user);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_shouldThrow_whenNotFound() {
        // given
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.getUserByEmail("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found by email");
    }

    // ===================== getUserByUUID =====================

    @Test
    void getUserByUUID_shouldReturnUser_whenExists() {
        // given
        User user = buildUser();
        when(userRepository.findByUuid(USER_UUID)).thenReturn(Optional.of(user));

        // when
        User result = userService.getUserByUUID(USER_UUID);

        // then
        assertThat(result).isSameAs(user);
        verify(userRepository).findByUuid(USER_UUID);
    }

    @Test
    void getUserByUUID_shouldThrow_whenNotFound() {
        // given
        when(userRepository.findByUuid(USER_UUID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.getUserByUUID(USER_UUID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found by uuid");
    }

    // ===================== getUserByParams =====================

    @Test
    void getUserByParams_shouldReturnPageFromRepository() {
        // given
        Specification<User> spec = Specification.where((root, query, cb) -> cb.conjunction());
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(buildUser()));

        when(userRepository.findAll(spec, pageRequest)).thenReturn(page);

        // when
        Page<User> result = userService.getUserByParams(spec, pageRequest);

        // then
        assertThat(result).isSameAs(page);
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findAll(spec, pageRequest);
    }

    @Test
    void getUserByParams_shouldReturnEmptyPage_whenNothingFound() {
        // given
        Specification<User> spec = Specification.where((root, query, cb) -> cb.conjunction());
        PageRequest pageRequest = PageRequest.of(0, 20);

        when(userRepository.findAll(spec, pageRequest)).thenReturn(Page.empty());

        // when
        Page<User> result = userService.getUserByParams(spec, pageRequest);

        // then
        assertThat(result).isEmpty();
        assertThat(result.getContent()).isEmpty();
    }

    // ===================== updateUser =====================

    @Test
    void updateUser_shouldSaveAndReturnUser() {
        // given
        User user = buildUser();
        user.setEmail("updated@example.com");

        when(userRepository.save(user)).thenReturn(user);

        // when
        User result = userService.updateUser(user);

        // then
        assertThat(result).isSameAs(user);
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        verify(userRepository).save(user);
    }

    // ===================== deleteUser =====================

    @Test
    void deleteUser_shouldDelegateToRepository() {
        // given
        User user = buildUser();

        // when
        userService.deleteUser(user);

        // then
        verify(userRepository).delete(user);
    }
}