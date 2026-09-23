package petproject.javapks.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(name = "storage_id", nullable = false, updatable = false)
    private UUID storageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource", nullable = false)
    private Resource resource;
}
