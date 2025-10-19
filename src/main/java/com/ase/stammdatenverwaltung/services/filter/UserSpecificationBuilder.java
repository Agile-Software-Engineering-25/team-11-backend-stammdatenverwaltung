package com.ase.stammdatenverwaltung.services.filter;

import com.ase.stammdatenverwaltung.dto.FilterCriterionDTO;
import com.ase.stammdatenverwaltung.dto.UserFilterRequestDTO;
import com.ase.stammdatenverwaltung.entities.Person;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserSpecificationBuilder {

  public Specification<Person> buildSpecification(UserFilterRequestDTO filterRequest) {
    return (root, query, cb) -> {
      if (filterRequest == null
          || filterRequest.getFilter() == null
          || filterRequest.getFilter().isEmpty()) {
        return cb.conjunction(); // No filters, return all
      }

      List<Predicate> predicates = new ArrayList<>();

      for (FilterCriterionDTO criterion : filterRequest.getFilter()) {
        String[] keys = criterion.getKey().split("\\.");
        Path<?> path = root;
        Join<?, ?> join = null;

        for (int i = 0; i < keys.length - 1; i++) {
          if (join == null) {
            join = root.join(keys[i], JoinType.LEFT);
          } else {
            join = join.join(keys[i], JoinType.LEFT);
          }
        }

        if (join != null) {
          path = join.get(keys[keys.length - 1]);
        } else {
          path = root.get(keys[0]);
        }

        predicates.add(createPredicate(cb, path, criterion));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private Predicate createPredicate(
      CriteriaBuilder cb, Path<?> path, FilterCriterionDTO criterion) {
    switch (criterion.getOperator().toLowerCase()) {
      case "eq":
        return cb.equal(path, criterion.getValue());
      case "in":
        return path.in((List<?>) criterion.getValue());
      case "gt":
        return cb.greaterThan(path.as(String.class), criterion.getValue().toString());
      case "lt":
        return cb.lessThan(path.as(String.class), criterion.getValue().toString());
      case "like":
        return cb.like(path.as(String.class), "%" + criterion.getValue() + "%");
      default:
        throw new IllegalArgumentException("Unsupported operator: " + criterion.getOperator());
    }
  }
}
