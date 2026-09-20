package petproject.javapks.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import petproject.javapks.config.MinioConfig;
import petproject.javapks.dto.response.StoredFile;
import petproject.javapks.exception.StorageException;
import petproject.javapks.exception.StoredFileNotFoundException;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private static final String KEY_PREFIX = "files/";
    private static final String NOT_FOUND_CODE = "NoSuchKey";

    private final MinioClient minioClient;
    private final MinioConfig.MinioProperties minioProperties;

    public UUID upload(InputStream data, long size, String contentType) {
        UUID id = UUID.randomUUID();
        String key = keyOf(id);
        String type = contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.bucketName())
                            .object(key)
                            .stream(data, size, -1)
                            .contentType(type)
                            .build()
            );
        } catch (Exception e) {
            log.warn("Upload failed for key {}: {}", key, e.getMessage());
            throw new StorageException("Failed to upload file", e);
        }
        log.info("Uploaded file with key {}", key);
        return id;
    }

    // Вызывать через try!
    public StoredFile download(UUID id) {
        String key = keyOf(id);
        StatObjectResponse stat;
        try {
            stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.bucketName())
                            .object(key)
                            .build()
            );
        } catch (ErrorResponseException e) {
            if (isNotFound(e)) {
                throw new StoredFileNotFoundException("File not found: " + id);
            }
            log.warn("Stat failed for key {}: {}", key, e.getMessage());
            throw new StorageException("Failed to read file metadata", e);
        } catch (Exception e) {
            log.warn("Stat failed for key {}: {}", key, e.getMessage());
            throw new StorageException("Failed to read file metadata", e);
        }
        String type = stat.contentType() != null
                ? stat.contentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        InputStream content;
        try {
            content = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.bucketName())
                            .object(key)
                            .build()
            );
        } catch (ErrorResponseException e) {
            if (isNotFound(e)) {
                throw new StoredFileNotFoundException("File not found: " + id);
            }
            log.warn("Download failed for key {}: {}", key, e.getMessage());
            throw new StorageException("Failed to download file", e);
        } catch (Exception e) {
            log.warn("Download failed for key {}: {}", key, e.getMessage());
            throw new StorageException("Failed to download file", e);
        }
        return new StoredFile(content, type, stat.size());
    }

    private static String keyOf(UUID id) {
        return KEY_PREFIX + id;
    }

    private static boolean isNotFound(ErrorResponseException e) {
        return e.errorResponse() != null && NOT_FOUND_CODE.equals(e.errorResponse().code());
    }
}
