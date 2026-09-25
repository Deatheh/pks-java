package petproject.javapks.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class File implements Persistable<UUID> {
    @Id
    @Column(updatable = false)
    private UUID uuid;

    // FileService pre-assigns uuid (it doubles as the MinIO object key), so
    // without Persistable save() would merge() a non-existent row and fail
    // with StaleObjectStateException.
    @Transient
    private boolean isNew = true;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource", nullable = false)
    private Resource resource;

    @Override
    public UUID getId() {
        return uuid;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        isNew = false;
    }
}
