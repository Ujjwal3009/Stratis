-- Standardize Auditing and Extra Soft Deletes
-- Version: 20

-- Add Auditing Columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by_id BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_by_id BIGINT;

ALTER TABLE questions ADD COLUMN IF NOT EXISTS created_by_id BIGINT;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS updated_by_id BIGINT;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE tests ADD COLUMN IF NOT EXISTS created_by_id BIGINT;
ALTER TABLE tests ADD COLUMN IF NOT EXISTS updated_by_id BIGINT;

ALTER TABLE test_attempts ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE test_attempts ADD COLUMN IF NOT EXISTS created_by_id BIGINT;
ALTER TABLE test_attempts ADD COLUMN IF NOT EXISTS updated_by_id BIGINT;
UPDATE test_attempts SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE test_attempts ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE pdf_documents ADD COLUMN IF NOT EXISTS created_by_id BIGINT;
ALTER TABLE pdf_documents ADD COLUMN IF NOT EXISTS updated_by_id BIGINT;

ALTER TABLE subjects ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS created_by_id BIGINT;
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS updated_by_id BIGINT;

ALTER TABLE topics ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE topics ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE topics ADD COLUMN IF NOT EXISTS created_by_id BIGINT;
ALTER TABLE topics ADD COLUMN IF NOT EXISTS updated_by_id BIGINT;

-- Migrate values for better audit trail consistency
UPDATE questions SET created_by_id = created_by WHERE created_by_id IS NULL;
UPDATE tests SET created_by_id = created_by WHERE created_by_id IS NULL;
UPDATE pdf_documents SET created_by_id = uploaded_by WHERE created_by_id IS NULL;
UPDATE test_attempts SET created_by_id = user_id WHERE created_by_id IS NULL;

-- Indices
CREATE INDEX idx_questions_deleted ON questions(deleted) WHERE deleted = false;
