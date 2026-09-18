package specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import petproject.javapks.dto.request.admin.UserFilterRequest;
import petproject.javapks.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {
    public static Specification<User> withFilters(UserFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтр по email (поиск по вхождению, регистронезависимый)
            if (StringUtils.hasText(filter.email())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + filter.email().toLowerCase() + "%"
                ));
            }

            // Фильтр по имени (поиск по вхождению)
            if (StringUtils.hasText(filter.firstName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + filter.firstName().toLowerCase() + "%"
                ));
            }

            // Фильтр по фамилии (поиск по вхождению)
            if (StringUtils.hasText(filter.lastName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + filter.lastName().toLowerCase() + "%"
                ));
            }

            // Фильтр по роли (точное совпадение)
            if (filter.role() != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), filter.role()));
            }

            // Фильтр по статусу enabled (точное совпадение)
            if (filter.enabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), filter.enabled()));
            }

            // Фильтр по дате создания (точное совпадение)
            // Примечание: если нужен диапазон (от/до), лучше добавить в record поля createdAtFrom и createdAtTo
            if (filter.createdAt() != null) {
                predicates.add(criteriaBuilder.equal(root.get("createdAt"), filter.createdAt()));
            }

            // Фильтр по дате обновления (точное совпадение)
            if (filter.updatedAt() != null) {
                predicates.add(criteriaBuilder.equal(root.get("updatedAt"), filter.updatedAt()));
            }

            // Объединяем все условия через AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
