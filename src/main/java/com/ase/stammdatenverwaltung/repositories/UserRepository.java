package com.ase.stammdatenverwaltung.repositories;

import com.ase.stammdatenverwaltung.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Indicates that this is a repository component
public interface UserRepository extends JpaRepository<User, Long> {
  // Spring Data JPA automatically provides CRUD methods (save, findById, findAll, deleteById, etc.)
  // You can also define custom query methods here:
  Optional<User> findByUsername(String username);
}
