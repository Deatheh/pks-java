package petproject.javapks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import petproject.javapks.exception.ResourceNotFoundException;
import petproject.javapks.model.Resource;
import petproject.javapks.repository.ResourceRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public List<Resource> getAll() {
        return resourceRepository.findAll();
    }

    public Resource getByUuid(UUID uuid) {
        return resourceRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found by uuid"));
    }
}
