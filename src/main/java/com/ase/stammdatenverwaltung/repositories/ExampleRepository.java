package com.ase.stammdatenverwaltung.repositories;

import com.ase.stammdatenverwaltung.entities.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for Example entities. */
@Repository
public interface ExampleRepository extends JpaRepository<Example, Long> {}
