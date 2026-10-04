package petproject.javapks.config;

import io.minio.MinioClient;
import jakarta.validation.constraints.NotBlank;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Creates the {@link MinioClient} used for object storage.
 * <p></p>
 * Warning <p>Requires {@code com.squareup.okhttp3:okhttp} as an explicit compile
 * dependency. {@code MinioClient.Builder} inherits from
 * {@code io.minio.BaseMinioClient.Builder}, whose API exposes OkHttp types
 * ({@code httpClient(OkHttpClient)}), so javac must be able to resolve
 * {@code okhttp3.HttpUrl} to compile any code that merely touches
 * {@code MinioClient}. MinIO only brings OkHttp in transitively, which is
 * exactly the kind of edge that disappears from an IDE project model.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MinioConfig.MinioProperties.class)
public class MinioConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(30);

    private final MinioProperties properties;

    public MinioConfig(MinioProperties properties) {
        this.properties = properties;
    }


    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .httpClient(httpClient())
                .build();
    }

    private OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT)
                .readTimeout(READ_TIMEOUT)
                .writeTimeout(WRITE_TIMEOUT)
                .build();
    }

    @Validated
    @ConfigurationProperties(prefix = "minio")
    public record MinioProperties(
            @NotBlank String endpoint,
            @NotBlank String accessKey,
            @NotBlank String secretKey,
            @NotBlank String bucketName) {
    }
}
