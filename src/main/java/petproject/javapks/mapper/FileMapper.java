package petproject.javapks.mapper;

import org.mapstruct.Mapper;
import petproject.javapks.dto.response.FileDto;
import petproject.javapks.model.File;

@Mapper(componentModel = "spring")
public interface FileMapper {
    FileDto toDto(File file);
}
