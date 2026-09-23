package petproject.javapks.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import petproject.javapks.dto.request.resource.ResourceFilterRequest;
import petproject.javapks.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class ResourceSpecification {

    public static Specification<Resource> withFilters(ResourceFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.title() != null && !filter.title().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + filter.title().toLowerCase() + "%"));
            }

            if (filter.description() != null && !filter.description().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("description")),
                        "%" + filter.description().toLowerCase() + "%"));
            }

            if (filter.createdAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.createdAt()));
            }

            if (filter.updatedAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), filter.updatedAt()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
