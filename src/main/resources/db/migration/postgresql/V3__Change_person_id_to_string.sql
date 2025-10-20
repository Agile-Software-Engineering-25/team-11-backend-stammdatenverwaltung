-- V3__Change_person_id_to_string.sql

-- Drop foreign key constraints
ALTER TABLE lecturers DROP CONSTRAINT IF EXISTS fk_lecturers_person_id;
ALTER TABLE students DROP CONSTRAINT IF EXISTS fk_students_person_id;
ALTER TABLE employees DROP CONSTRAINT IF EXISTS fk_employees_person_id;

-- Alter the person_id column types with explicit casting
ALTER TABLE students ALTER COLUMN person_id TYPE VARCHAR(255) USING person_id::VARCHAR;
ALTER TABLE employees ALTER COLUMN person_id TYPE VARCHAR(255) USING person_id::VARCHAR;
ALTER TABLE lecturers ALTER COLUMN person_id TYPE VARCHAR(255) USING person_id::VARCHAR;

-- Adjust persons.id
-- Drop the identity property (required in PostgreSQL)
ALTER TABLE persons ALTER COLUMN id DROP IDENTITY IF EXISTS;

-- Drop default or sequence if any
ALTER TABLE persons ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS persons_id_seq;

-- Convert the id column type to VARCHAR
ALTER TABLE persons ALTER COLUMN id TYPE VARCHAR(255) USING id::VARCHAR;

-- Re-add foreign key constraints
ALTER TABLE students ADD CONSTRAINT fk_students_person_id
    FOREIGN KEY (person_id) REFERENCES persons (id) ON DELETE CASCADE;

ALTER TABLE employees ADD CONSTRAINT fk_employees_person_id
    FOREIGN KEY (person_id) REFERENCES persons (id) ON DELETE CASCADE;

-- NOTE: lecturers.person_id should most likely reference persons(id), not employees(person_id)
-- Fixing that unless it's intentional:
ALTER TABLE lecturers ADD CONSTRAINT fk_lecturers_person_id
    FOREIGN KEY (person_id) REFERENCES persons (id) ON DELETE CASCADE;
