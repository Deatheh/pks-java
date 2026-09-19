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
 * <p>
 * </p>
 * Warning
 * <p>
 * Requires {@code com.squareup.okhttp3:okhttp} as an explicit compile
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

    /**
     * Constructor injection: keeps the bean immutable and its dependencies
     * mandatory.
     */
    public MinioConfig(MinioProperties properties) {
        this.properties = properties;
    }

    /**
     * The client is a thread-safe singleton; inject it wherever you need it.
     * It implements {@code AutoCloseable}, so Spring will close it (and the
     * underlying OkHttp connection pool) on context shutdown via the inferred
     * destroy method.
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .httpClient(httpClient())
                .build();
    }

    /**
     * MinIO's built-in default HTTP client is not tuned for interactive
     * request/response workloads, so supply one with explicit timeouts.
     * Deliberately kept private rather than exposed as a {@code @Bean}:
     * publishing a bare {@link OkHttpClient} into the context invites
     * collisions with other libraries that define one.
     */
    private OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT)
                .readTimeout(READ_TIMEOUT)
                .writeTimeout(WRITE_TIMEOUT)
                .build();
    }

    /**
     * Type-safe binding for the {@code minio.*} keys.
     *
     * <p>
     * Spring Boot 4 binds records natively via their canonical constructor,
     * so no Lombok, no setters and no no-arg constructor are needed. Relaxed
     * binding applies: {@code minio.access-key}, {@code minio.accessKey} and
     * {@code MINIO_ACCESSKEY} all map to {@code accessKey}.
     *
     * <p>
     * {@code @Validated} plus the component constraints means a missing or
     * blank value fails fast at startup with a clear
     * {@code BindValidationException} instead of a {@code NullPointerException}
     * somewhere deep inside the MinIO SDK on the first request.
     */
    @Validated
    @ConfigurationProperties(prefix = "minio")
    public record MinioProperties(
            @NotBlank String endpoint,
            @NotBlank String accessKey,
            @NotBlank String secretKey,
            @NotBlank String bucketName) {
    }
}
