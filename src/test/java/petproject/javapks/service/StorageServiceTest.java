package petproject.javapks.service;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import petproject.javapks.config.MinioConfig;
import petproject.javapks.exception.StorageException;
import petproject.javapks.exception.StoredFileNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    private static final String BUCKET = "test-bucket";

    @Mock
    private MinioClient minioClient;

    private final MinioConfig.MinioProperties properties =
            new MinioConfig.MinioProperties("http://localhost:9000", "ak", "sk", BUCKET);

    private StorageService service() {
        return new StorageService(minioClient, properties);
    }

    @Test
    void uploadReturnsUuidAndStoresUnderFilesPrefix() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenReturn(mock(ObjectWriteResponse.class));
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);

        String id = service().upload(new ByteArrayInputStream(data), data.length, "text/plain");

        UUID.fromString(id);
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals("files/" + id, captor.getValue().object());
        assertEquals("text/plain", captor.getValue().contentType());
    }

    @Test
    void uploadWrapsSdkFailure() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new IOException("boom"));

        assertThrows(StorageException.class, () ->
                service().upload(new ByteArrayInputStream(new byte[0]), 0, "text/plain"));
    }

    @Test
    void downloadReturnsContentTypeSizeAndBytes() throws Exception {
        String id = UUID.randomUUID().toString();
        byte[] data = {1, 2, 3, 4};
        StatObjectResponse stat = mock(StatObjectResponse.class);
        when(stat.contentType()).thenReturn("image/png");
        when(stat.size()).thenReturn((long) data.length);
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(stat);
        GetObjectResponse object = mock(GetObjectResponse.class);
        when(object.readAllBytes()).thenReturn(data);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(object);

        StorageService.StoredFile file = service().download(id);

        assertEquals("image/png", file.contentType());
        assertEquals(data.length, file.size());
        assertArrayEquals(data, file.content().readAllBytes());
    }

    @Test
    void downloadMissingFileThrowsNotFound() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class)))
                .thenThrow(notFoundException());

        assertThrows(StoredFileNotFoundException.class, () ->
                service().download(UUID.randomUUID().toString()));
    }

    @Test
    void downloadInvalidIdThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service().download("not-a-uuid"));
    }

    private static ErrorResponseException notFoundException() {
        ErrorResponse response = new ErrorResponse(
                "NoSuchKey", "not found", BUCKET, "files/id", "/files/id", "req-id", "host-id");
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("http://localhost:9000/" + BUCKET)
                .build();
        okhttp3.Response httpResponse = new okhttp3.Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(404)
                .message("Not Found")
                .build();
        return new ErrorResponseException(response, httpResponse, null);
    }
}
