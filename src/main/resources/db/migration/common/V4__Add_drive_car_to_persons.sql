-- Add drives_car column to persons table for parking service feature
-- This boolean field indicates whether a person drives a car to the institution
-- Default value is false as specified in the acceptance criteria

ALTER TABLE persons
ADD COLUMN drives_car BOOLEAN NOT NULL DEFAULT false;

-- Add constraint to enforce boolean values
ALTER TABLE persons
ADD CONSTRAINT chk_drives_car CHECK (drives_car IN (true, false));

-- Add comment for documentation
COMMENT ON COLUMN persons.drives_car IS 'Indicates whether the person drives a car to the institution (default: false) for parking service feature';
