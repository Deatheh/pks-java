package petproject.javapks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import petproject.javapks.dto.request.admin.RegisterRequest;
import petproject.javapks.dto.request.admin.UpdateUserRequest;
import petproject.javapks.dto.request.admin.UserFilterRequest;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.exception.EmailAlreadyExistsException;
import petproject.javapks.exception.UserNotFoundException;
import petproject.javapks.mapper.UserMapper;
import petproject.javapks.model.Role;
import petproject.javapks.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

        @Mock
        private UserMapper userMapper;

        @Mock
        private UserService userService;

        @InjectMocks
        private AdminService adminService;

        private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

        private User buildUser() {
                User user = new User();
                user.setUuid(USER_UUID);
                user.setEmail("test@example.com");
                user.setPassword("encodedPassword");
                user.setRole(Role.USER);
                user.setEnabled(true);
                user.setFirstName("Johnny");
                user.setLastName("Doeman");
                user.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));
                user.setUpdatedAt(LocalDateTime.of(2024, 1, 2, 12, 0));
                return user;
        }

        private UserDto buildUserDto() {
                return new UserDto(
                                USER_UUID,
                                "test@example.com",
                                Role.USER,
                                "Johnny",
                                "Doeman",
                                true,
                                LocalDateTime.of(2024, 1, 1, 12, 0),
                                LocalDateTime.of(2024, 1, 2, 12, 0));
        }

        // ===================== createUser =====================

        @Test
        void createUser_shouldReturnDto_whenValidRequest() {
                // given
                RegisterRequest request = new RegisterRequest(
                                "test@example.com",
                                "password123",
                                Role.USER,
                                "Johnny",
                                "Doeman");
                User entity = buildUser();
                User saved = buildUser();
                UserDto expected = buildUserDto();

                when(userMapper.toEntity(request)).thenReturn(entity);
                when(userService.createUser(entity)).thenReturn(saved);
                when(userMapper.toDto(saved)).thenReturn(expected);

                // when
                UserDto result = adminService.createUser(request);

                // then
                assertThat(result).isEqualTo(expected);
                verify(userMapper).toEntity(request);
                verify(userService).createUser(entity);
                verify(userMapper).toDto(saved);
        }

        @Test
        void createUser_shouldThrow_whenEmailAlreadyExists() {
                // given
                RegisterRequest request = new RegisterRequest(
                                "test@example.com",
                                "password123",
                                Role.USER,
                                "Johnny",
                                "Doeman");
                User entity = buildUser();

                when(userMapper.toEntity(request)).thenReturn(entity);
                when(userService.createUser(entity))
                                .thenThrow(new EmailAlreadyExistsException("Email already in use"));

                // when / then
                assertThatThrownBy(() -> adminService.createUser(request))
                                .isInstanceOf(EmailAlreadyExistsException.class)
                                .hasMessageContaining("Email already in use");
        }

        // ===================== getUserByUUID / getUserByEmail =====================

        @Test
        void getUserByUUID_shouldReturnDto_whenExists() {
                // given
                User user = buildUser();
                UserDto expected = buildUserDto();

                when(userService.getUserByUUID(USER_UUID)).thenReturn(user);
                when(userMapper.toDto(user)).thenReturn(expected);

                // when
                UserDto result = adminService.getUserByUUID(USER_UUID);

                // then
                assertThat(result).isEqualTo(expected);
        }

        @Test
        void getUserByUUID_shouldThrow_whenNotFound() {
                // given
                when(userService.getUserByUUID(USER_UUID))
                                .thenThrow(new UserNotFoundException("User not found by uuid"));

                // when / then
                assertThatThrownBy(() -> adminService.getUserByUUID(USER_UUID))
                                .isInstanceOf(UserNotFoundException.class)
                                .hasMessageContaining("User not found by uuid");
        }

        @Test
        void getUserByEmail_shouldReturnDto_whenExists() {
                // given
                User user = buildUser();
                UserDto expected = buildUserDto();

                when(userService.getUserByEmail("test@example.com")).thenReturn(user);
                when(userMapper.toDto(user)).thenReturn(expected);

                // when
                UserDto result = adminService.getUserByEmail("test@example.com");

                // then
                assertThat(result).isEqualTo(expected);
        }

        // ===================== getUsers =====================

        @Test
        void getUsers_shouldReturnListOfDtos() {
                // given
                UserFilterRequest filter = new UserFilterRequest(
                                "test", Role.USER, null, null, null, null, null);
                User user = buildUser();
                UserDto dto = buildUserDto();
                Page<User> page = new PageImpl<>(List.of(user));

                when(userService.getUserByParams(any(Specification.class), any(PageRequest.class)))
                                .thenReturn(page);
                when(userMapper.toDto(user)).thenReturn(dto);

                // when
                List<UserDto> result = adminService.getUsers(filter, 0L, 20L, "createdAt", "desc");

                // then
                assertThat(result).hasSize(1).containsExactly(dto);
        }

        @Test
        void getUsers_shouldUseDefaults_whenCountAndOffsetAreNull() {
                // given
                UserFilterRequest filter = new UserFilterRequest(
                                null, null, null, null, null, null, null);
                Page<User> emptyPage = new PageImpl<>(List.of());

                when(userService.getUserByParams(any(Specification.class), any(PageRequest.class)))
                                .thenReturn(emptyPage);

                // when
                adminService.getUsers(filter, null, null, null, null);

                // then — проверяем, что в PageRequest ушли дефолтные 0/20
                ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
                verify(userService).getUserByParams(any(Specification.class), captor.capture());

                PageRequest pr = captor.getValue();
                assertThat(pr.getPageNumber()).isZero();
                assertThat(pr.getPageSize()).isEqualTo(20);
                assertThat(pr.getSort().getOrderFor("createdAt")).isNotNull();
                assertThat(pr.getSort().getOrderFor("createdAt").getDirection())
                                .isEqualTo(Sort.Direction.DESC);
        }

        @Test
        void getUsers_shouldFallbackToCreatedAt_whenSortByIsInvalid() {
                // given
                UserFilterRequest filter = new UserFilterRequest(
                                null, null, null, null, null, null, null);

                when(userService.getUserByParams(any(Specification.class), any(PageRequest.class)))
                                .thenReturn(new PageImpl<>(List.of()));

                // when — sortBy = "hackerField", sortDir = "asc"
                adminService.getUsers(filter, 0L, 20L, "hackerField", "asc");

                // then — createSort должен подменить поле на createdAt
                ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
                verify(userService).getUserByParams(any(Specification.class), captor.capture());

                Sort sort = captor.getValue().getSort();
                assertThat(sort.getOrderFor("hackerField")).isNull();
                assertThat(sort.getOrderFor("createdAt")).isNotNull();
                assertThat(sort.getOrderFor("createdAt").getDirection())
                                .isEqualTo(Sort.Direction.ASC);
        }

        @Test
        void getUsers_shouldAcceptSortByFieldFromFilter() {
                // given — "email" есть в UserFilterRequest, значит должен остаться как есть
                UserFilterRequest filter = new UserFilterRequest(
                                null, null, null, null, null, null, null);

                when(userService.getUserByParams(any(Specification.class), any(PageRequest.class)))
                                .thenReturn(new PageImpl<>(List.of()));

                // when
                adminService.getUsers(filter, 0L, 20L, "email", "asc");

                // then
                ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
                verify(userService).getUserByParams(any(Specification.class), captor.capture());

                Sort sort = captor.getValue().getSort();
                assertThat(sort.getOrderFor("email")).isNotNull();
                assertThat(sort.getOrderFor("email").getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        // ===================== updateUser =====================

        @Test
        void updateUser_shouldApplyChangesAndReturnDto() {
                // given
                UpdateUserRequest request = new UpdateUserRequest(
                                "updated@example.com",
                                false,
                                Role.ADMIN,
                                "Johnny",
                                "Doeman");
                User existing = buildUser();
                UserDto expected = new UserDto(
                                USER_UUID,
                                "updated@example.com",
                                Role.ADMIN,
                                "Johnny",
                                "Doeman",
                                false,
                                LocalDateTime.of(2024, 1, 1, 12, 0),
                                LocalDateTime.of(2024, 1, 2, 12, 0));

                when(userService.getUserByUUID(USER_UUID)).thenReturn(existing);
                when(userService.updateUser(existing)).thenReturn(existing);
                when(userMapper.toDto(existing)).thenReturn(expected);

                // when
                UserDto result = adminService.updateUser(USER_UUID, request);

                // then
                assertThat(result).isEqualTo(expected);
                assertThat(existing.getEmail()).isEqualTo("updated@example.com");
                assertThat(existing.getEnabled()).isFalse();
                assertThat(existing.getRole()).isEqualTo(Role.ADMIN);
                assertThat(existing.getFirstName()).isEqualTo("Johnny");
                assertThat(existing.getLastName()).isEqualTo("Doeman");
                verify(userService).updateUser(existing);
        }

        @Test
        void updateUser_shouldThrow_whenUserNotFound() {
                // given
                UpdateUserRequest request = new UpdateUserRequest(
                                "updated@example.com",
                                false,
                                Role.ADMIN,
                                "Johnny",
                                "Doeman");
                when(userService.getUserByUUID(USER_UUID))
                                .thenThrow(new UserNotFoundException("User not found by uuid"));

                // when / then
                assertThatThrownBy(() -> adminService.updateUser(USER_UUID, request))
                                .isInstanceOf(UserNotFoundException.class)
                                .hasMessageContaining("User not found by uuid");
        }

        // ===================== deleteUser =====================

        @Test
        void deleteUser_shouldFetchAndDelete() {
                // given
                User user = buildUser();
                when(userService.getUserByUUID(USER_UUID)).thenReturn(user);
                doNothing().when(userService).deleteUser(user);

                // when
                adminService.deleteUser(USER_UUID);

                // then
                verify(userService).getUserByUUID(USER_UUID);
                verify(userService).deleteUser(user);
        }

        @Test
        void deleteUser_shouldThrow_whenUserNotFound() {
                // given
                when(userService.getUserByUUID(USER_UUID))
                                .thenThrow(new UserNotFoundException("User not found by uuid"));

                // when / then
                assertThatThrownBy(() -> adminService.deleteUser(USER_UUID))
                                .isInstanceOf(UserNotFoundException.class)
                                .hasMessageContaining("User not found by uuid");
                verify(userService, org.mockito.Mockito.never()).deleteUser(any());
        }
}