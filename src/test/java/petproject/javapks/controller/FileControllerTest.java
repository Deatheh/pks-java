package petproject.javapks.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import petproject.javapks.exception.GlobalExceptionHandler;
import petproject.javapks.exception.StoredFileNotFoundException;
import petproject.javapks.service.StorageService;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private StorageService storageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FileController(storageService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void uploadReturnsCreatedWithId() throws Exception {
        String id = UUID.randomUUID().toString();
        when(storageService.upload(any(), anyLong(), eq("text/plain"))).thenReturn(id);
        MockMultipartFile file = new MockMultipartFile(
                "file", "hello.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void downloadReturnsBytesWithContentType() throws Exception {
        String id = UUID.randomUUID().toString();
        byte[] data = {1, 2, 3};
        when(storageService.download(id)).thenReturn(
                new StorageService.StoredFile(new ByteArrayInputStream(data), "image/png", data.length));

        mockMvc.perform(get("/api/v1/files/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(data));
    }

    @Test
    void downloadMissingFileReturnsNotFound() throws Exception {
        String id = UUID.randomUUID().toString();
        when(storageService.download(id))
                .thenThrow(new StoredFileNotFoundException("File not found: " + id));

        mockMvc.perform(get("/api/v1/files/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadWithoutFileReturnsBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/v1/files"))
                .andExpect(status().isBadRequest());
    }
}
