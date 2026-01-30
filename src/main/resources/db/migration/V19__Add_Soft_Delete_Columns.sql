-- Migration for Soft Deletes
-- Version: 19
-- Description: Adds deleted column for Test, TestAttempt, and PdfDocument entities

ALTER TABLE tests ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE test_attempts ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE pdf_documents ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX idx_tests_deleted ON tests(deleted) WHERE deleted = false;
CREATE INDEX idx_test_attempts_deleted ON test_attempts(deleted) WHERE deleted = false;
CREATE INDEX idx_pdf_documents_deleted ON pdf_documents(deleted) WHERE deleted = false;
