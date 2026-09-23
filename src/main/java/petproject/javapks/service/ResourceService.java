package petproject.javapks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import petproject.javapks.dto.request.resource.CreateResourceRequest;
import petproject.javapks.dto.request.resource.ResourceFilterRequest;
import petproject.javapks.dto.request.resource.UpdateResourceRequest;
import petproject.javapks.dto.response.ResourceDto;
import petproject.javapks.exception.ResourceNotFoundException;
import petproject.javapks.mapper.ResourceMapper;
import petproject.javapks.model.Resource;
import petproject.javapks.repository.ResourceRepository;
import petproject.javapks.specification.ResourceSpecification;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

    public ResourceDto createResource(CreateResourceRequest dto){
        return resourceMapper.toDto(resourceRepository.save(resourceMapper.toEntity(dto)));
    }

    public List<Resource> getAll() {
        return resourceRepository.findAll();
    }

    public Resource getByUuid(UUID uuid) {
        return resourceRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found by uuid"));
    }

    public List<ResourceDto> getResources(ResourceFilterRequest filter,
                                          Long offset,
                                          Long count,
                                          String sortBy,
                                          String sortDir) {
        Specification<Resource> spec = ResourceSpecification.withFilters(filter);

        int limit = (count != null && count > 0) ? count.intValue() : 20;
        int off = (offset != null && offset >= 0) ? offset.intValue() : 0;
        int pageNumber = limit > 0 ? off / limit : 0;

        Sort sort = createSort(sortBy, sortDir);
        PageRequest pageRequest = PageRequest.of(pageNumber, limit, sort);

        return resourceRepository.findAll(spec, pageRequest).getContent().stream()
                .map(resourceMapper::toDto)
                .toList();
    }

    public ResourceDto updateResource(UUID uuid, UpdateResourceRequest dto) {
        Resource resource = getByUuid(uuid);
        resourceMapper.updateEntity(dto, resource);
        return resourceMapper.toDto(resourceRepository.save(resource));
    }

    public void deleteResource(UUID uuid) {
        resourceRepository.delete(getByUuid(uuid));
    }

    private Sort createSort(String sortBy, String sortDir) {
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";

        List<String> allowedFields = Arrays.stream(ResourceFilterRequest.class.getRecordComponents())
                .map(component -> component.getName())
                .toList();

        if (!allowedFields.contains(sortField)) {
            sortField = "createdAt";
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, sortField);
    }

    public ResourceDto getResourceByUuid(UUID uuid){
        return resourceMapper.toDto(getByUuid(uuid));
    }
}
