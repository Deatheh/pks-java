package petproject.javapks.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import petproject.javapks.dto.request.resource.CreateResourceRequest;
import petproject.javapks.dto.request.resource.ResourceFilterRequest;
import petproject.javapks.dto.request.resource.UpdateResourceRequest;
import petproject.javapks.dto.response.ResourceDto;
import petproject.javapks.service.ExportService;
import petproject.javapks.service.ResourceService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<ResourceDto> createResource(
            @Valid @RequestBody CreateResourceRequest dto
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.createResource(dto));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ResourceDto> getResourceByUUID(
            @PathVariable UUID uuid
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(resourceService.getResourceByUuid(uuid));
    }

    @GetMapping
    public ResponseEntity<List<ResourceDto>> getResourcesByFilters(
            @ModelAttribute ResourceFilterRequest filter,
            @RequestParam(defaultValue = "0") Long offset,
            @RequestParam(defaultValue = "20") Long count,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(resourceService.getResources(
                filter,
                offset,
                count,
                sortBy,
                sortDir
        ));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ResourceDto> updateResource(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateResourceRequest dto
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(resourceService.updateResource(uuid, dto));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable UUID uuid
    ) {
        resourceService.deleteResource(uuid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
