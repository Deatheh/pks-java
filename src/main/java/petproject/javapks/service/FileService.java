package petproject.javapks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import petproject.javapks.model.File;
import petproject.javapks.repository.FileRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRepository fileRepository;

    public List<File> getAllByResourceUuid(UUID resourceUuid) {
        return fileRepository.findAllByResourceUuid(resourceUuid);
    }
}
