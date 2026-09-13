package petproject.javapks.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    // Потом востоновим для админки
    /*@Mapping(target = "uuid", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tests", ignore = true)
    @Mapping(target = "testAttempts", ignore = true)
    User toEntity(RegisterRequest registerRequest);*/
}
