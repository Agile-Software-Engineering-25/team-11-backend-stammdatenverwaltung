-- V3__Change_person_id_to_string.sql

-- Drop foreign key constraints to allow for type changes
ALTER TABLE lecturers DROP CONSTRAINT fk_lecturers_person_id;
ALTER TABLE students DROP CONSTRAINT fk_students_person_id;
ALTER TABLE employees DROP CONSTRAINT fk_employees_person_id;

-- Alter the person id column in child tables
ALTER TABLE students ALTER COLUMN person_id VARCHAR(255);
ALTER TABLE employees ALTER COLUMN person_id VARCHAR(255);
ALTER TABLE lecturers ALTER COLUMN person_id VARCHAR(255);

-- Alter the primary key in the persons table
-- First, remove the identity generation
ALTER TABLE persons ALTER COLUMN id DROP IDENTITY;
-- Then, change the type
ALTER TABLE persons ALTER COLUMN id VARCHAR(255);

-- Re-add the foreign key constraints
ALTER TABLE students ADD CONSTRAINT fk_students_person_id FOREIGN KEY (person_id) REFERENCES persons (id) ON DELETE CASCADE;
ALTER TABLE employees ADD CONSTRAINT fk_employees_person_id FOREIGN KEY (person_id) REFERENCES persons (id) ON DELETE CASCADE;
ALTER TABLE lecturers ADD CONSTRAINT fk_lecturers_person_id FOREIGN KEY (person_id) REFERENCES employees (person_id) ON DELETE CASCADE;
