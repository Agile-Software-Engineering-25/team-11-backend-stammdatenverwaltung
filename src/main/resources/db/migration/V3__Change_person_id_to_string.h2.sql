-- H2 version (simpler, no identity handling)
ALTER TABLE lecturers DROP CONSTRAINT IF EXISTS fk_lecturers_person_id;
ALTER TABLE students DROP CONSTRAINT IF EXISTS fk_students_person_id;
ALTER TABLE employees DROP CONSTRAINT IF EXISTS fk_employees_person_id;

ALTER TABLE students ALTER COLUMN person_id VARCHAR(255);
ALTER TABLE employees ALTER COLUMN person_id VARCHAR(255);
ALTER TABLE lecturers ALTER COLUMN person_id VARCHAR(255);

ALTER TABLE persons ALTER COLUMN id VARCHAR(255);

ALTER TABLE students ADD CONSTRAINT fk_students_person_id FOREIGN KEY (person_id) REFERENCES persons (id) ON DELETE CASCADE;
ALTER TABLE employees ADD CONSTRAINT fk_employees_person_id FOREIGN KEY (person_id) REFERENCES persons (id) ON DELETE CASCADE;
ALTER TABLE lecturers ADD CONSTRAINT fk_lecturers_person_id FOREIGN KEY (person_id) REFERENCES persons (id) ON DELETE CASCADE;
