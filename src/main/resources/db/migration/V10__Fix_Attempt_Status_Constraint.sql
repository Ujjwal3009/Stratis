-- Drop the restrictive check constraint on status
ALTER TABLE test_attempts DROP CONSTRAINT IF EXISTS chk_status;

-- Re-add with proper values matching the Java Enum
ALTER TABLE test_attempts ADD CONSTRAINT chk_status 
    CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'ABANDONED', 'SUBMITTED', 'EVALUATED'));
