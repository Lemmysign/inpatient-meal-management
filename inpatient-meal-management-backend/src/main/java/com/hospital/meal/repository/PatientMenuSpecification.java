package com.hospital.meal.repository;

import com.hospital.meal.model.menu.PatientMenu;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PatientMenuSpecification {

    public static Specification<PatientMenu> withFilters(
            List<UUID> menuGroupIds,
            List<UUID> dieticianIds,
            String searchTerm,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean isActive) {

        return (root, query, cb) -> {

            // Separate count query from data query to avoid fetch join issues
            boolean isCountQuery = query.getResultType() == Long.class;

            Join<Object, Object> patient;
            Join<Object, Object> menuGroup;
            Join<Object, Object> dietician;

            if (isCountQuery) {
                patient = root.join("patient", JoinType.INNER);
                menuGroup = root.join("menuGroup", JoinType.INNER);
                dietician = root.join("assignedByDietician", JoinType.INNER);
            } else {
                query.distinct(true);
                patient = (Join<Object, Object>) root.fetch("patient", JoinType.INNER);
                menuGroup = (Join<Object, Object>) root.fetch("menuGroup", JoinType.INNER);
                dietician = (Join<Object, Object>) root.fetch("assignedByDietician", JoinType.INNER);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (menuGroupIds != null && !menuGroupIds.isEmpty()) {
                predicates.add(menuGroup.get("id").in(menuGroupIds));
            }

            if (dieticianIds != null && !dieticianIds.isEmpty()) {
                predicates.add(dietician.get("id").in(dieticianIds));
            }

            if (searchTerm != null && !searchTerm.isBlank()) {
                String pattern = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(patient.get("name")), pattern),
                        cb.like(cb.lower(patient.get("uhid")), pattern)
                ));
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("validFrom"), dateFrom));
            }

            if (dateTo != null) {
                predicates.add(cb.or(
                        cb.lessThanOrEqualTo(root.get("validUntil"), dateTo),
                        cb.isNull(root.get("validUntil"))
                ));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}