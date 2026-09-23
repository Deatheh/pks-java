package petproject.javapks.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import petproject.javapks.dto.request.resource.CreateResourceRequest;
import petproject.javapks.dto.request.resource.UpdateResourceRequest;
import petproject.javapks.dto.response.ResourceDto;
import petproject.javapks.model.Resource;

@Mapper(componentModel = "spring", uses = {FileMapper.class})
public interface ResourceMapper {

    ResourceDto toDto(Resource resource);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "files", ignore = true)
    Resource toEntity(CreateResourceRequest createResourceRequest);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "files", ignore = true)
    void updateEntity(UpdateResourceRequest dto, @MappingTarget Resource resource);
}
