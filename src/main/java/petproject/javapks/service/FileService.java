package petproject.javapks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import petproject.javapks.dto.response.FileDto;
import petproject.javapks.dto.response.StoredFile;
import petproject.javapks.exception.StorageException;
import petproject.javapks.exception.StoredFileNotFoundException;
import petproject.javapks.mapper.FileMapper;
import petproject.javapks.model.File;
import petproject.javapks.model.Resource;
import petproject.javapks.repository.FileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final FileMapper fileMapper;
    private final StorageService storageService;
    private final ResourceService resourceService;

    public List<File> getAllByResourceUuid(UUID resourceUuid) {
        return fileRepository.findAllByResourceUuid(resourceUuid);
    }

    @Transactional
    public FileDto upload(UUID resourceUuid, MultipartFile multipartFile) {
        Resource resource = resourceService.getByUuid(resourceUuid);

        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        // Генерируем id заранее: он же пойдёт как PK в БД и как ключ в MinIO.
        UUID fileId = UUID.randomUUID();

        try (InputStream in = multipartFile.getInputStream()) {
            storageService.upload(
                    fileId,
                    in,
                    multipartFile.getSize(),
                    multipartFile.getContentType()
            );
        } catch (IOException e) {
            throw new StorageException("Failed to read uploaded file", e);
        }

        File file = new File();
        file.setUuid(fileId);
        file.setName(multipartFile.getOriginalFilename() != null
                ? multipartFile.getOriginalFilename()
                : fileId.toString());
        file.setContentType(multipartFile.getContentType() != null
                ? multipartFile.getContentType()
                : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
        file.setSize(multipartFile.getSize());
        file.setResource(resource);

        return fileMapper.toDto(fileRepository.save(file));
    }

    /**
     * Возвращает поток из MinIO + метаданные.
     * Вызывающий обязан закрыть {@link StoredFile} (try-with-resources).
     */
    @Transactional(readOnly = true)
    public StoredFile download(UUID resourceUuid, UUID fileUuid) {
        File file = getOwnedFile(resourceUuid, fileUuid);
        StoredFile stored = storageService.download(file.getUuid());

        // Доверяем contentType/size из БД — там ровно то, что загружал пользователь.
        return new StoredFile(stored.content(), file.getContentType(), file.getSize());
    }

    @Transactional
    public void delete(UUID resourceUuid, UUID fileUuid) {
        File file = getOwnedFile(resourceUuid, fileUuid);

        // Сначала MinIO, потом БД: если MinIO упадёт — запись останется,
        // повторный запрос сможет удалить снова. Наоборот — «сирота» в бакете.
        storageService.delete(file.getUuid());

        fileRepository.delete(file);
    }

    public File getOwnedFile(UUID resourceUuid, UUID fileUuid) {
        File file = fileRepository.findById(fileUuid)
                .orElseThrow(() -> new StoredFileNotFoundException(
                        "File not found: " + fileUuid));

        if (!file.getResource().getUuid().equals(resourceUuid)) {
            // Намеренно 404, а не 403: не палим существование чужого файла.
            throw new StoredFileNotFoundException(
                    "File " + fileUuid + " does not belong to resource " + resourceUuid);
        }
        return file;
    }
}