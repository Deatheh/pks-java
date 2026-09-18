package petproject.javapks.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import petproject.javapks.dto.request.admin.RegisterRequest;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest registerRequest);
}
