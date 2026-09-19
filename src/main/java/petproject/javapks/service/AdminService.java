package petproject.javapks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import petproject.javapks.dto.request.admin.RegisterRequest;
import petproject.javapks.dto.request.admin.UpdateUserRequest;
import petproject.javapks.dto.request.admin.UserFilterRequest;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.mapper.UserMapper;
import petproject.javapks.model.User;
import specification.UserSpecification;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserMapper userMapper;
    private final UserService userService;

    public UserDto createUser(RegisterRequest dto) {
        User user = userMapper.toEntity(dto);
        return userMapper.toDto(userService.createUser(user));
    }

    public UserDto getUserByUUID(UUID uuid) {
        return userMapper.toDto(userService.getUserByUUID(uuid));
    }

    public UserDto getUserByEmail(String email) {
        return userMapper.toDto(userService.getUserByEmail(email));
    }

    public List<UserDto> getUsers(UserFilterRequest filter,
            Long offset,
            Long count,
            String sortBy,
            String sortDir) {
        Specification<User> spec = UserSpecification.withFilters(filter);

        int limit = (count != null && count > 0) ? count.intValue() : 20;
        int off = (offset != null && offset >= 0) ? offset.intValue() : 0;

        int pageNumber = limit > 0 ? off / limit : 0;

        Sort sort = createSort(sortBy, sortDir);

        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                limit,
                sort);

        return userService.getUserByParams(spec, pageRequest).getContent().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto updateUser(UUID uuid, UpdateUserRequest dto) {
        User user = userService.getUserByUUID(uuid);
        user.setEmail(dto.email());
        user.setEnabled(dto.enabled());
        user.setRole(dto.role());
        user.setFirstName(dto.firstname());
        user.setLastName(dto.lastname());
        return userMapper.toDto(userService.updateUser(user));
    }

    public void deleteUser(UUID uuid) {
        userService.deleteUser(userService.getUserByUUID(uuid));
    }

    private Sort createSort(String sortBy, String sortDir) {
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";

        // Этот клохоз надо поменять
        List<String> allowedFields = Arrays.stream(UserFilterRequest.class.getRecordComponents())
                .map(component -> component.getName())
                .toList();

        if (!allowedFields.contains(sortField)) {
            sortField = "createdAt";
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(direction, sortField);
    }
}
