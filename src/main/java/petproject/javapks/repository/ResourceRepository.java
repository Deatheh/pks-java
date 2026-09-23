package petproject.javapks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import petproject.javapks.model.Resource;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> ,
        JpaSpecificationExecutor<Resource> {
    Optional<Resource> findResourceByUuid(UUID uuid);
    boolean existsByUuid(UUID uuid);
}
