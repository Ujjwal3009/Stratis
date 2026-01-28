-- Add Indexes, Constraints, and Triggers
-- Version: 2
-- Description: Performance optimizations and data integrity constraints

-- ============================================
-- Composite Indexes for Common Queries
-- ============================================

CREATE INDEX idx_test_attempts_user_test ON test_attempts(user_id, test_id);
CREATE INDEX idx_test_attempts_user_status ON test_attempts(user_id, status);

-- ============================================
-- Check Constraints for Data Integrity
-- ============================================

ALTER TABLE questions ADD CONSTRAINT chk_difficulty_level 
    CHECK (difficulty_level IN ('EASY', 'MEDIUM', 'HARD'));

ALTER TABLE questions ADD CONSTRAINT chk_question_type 
    CHECK (question_type IN ('MCQ', 'SUBJECTIVE', 'TRUE_FALSE'));

ALTER TABLE users ADD CONSTRAINT chk_role 
    CHECK (role IN ('USER', 'ADMIN', 'INSTRUCTOR'));

ALTER TABLE test_attempts ADD CONSTRAINT chk_status 
    CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'EVALUATED'));

ALTER TABLE tests ADD CONSTRAINT chk_duration_positive 
    CHECK (duration_minutes > 0);

ALTER TABLE tests ADD CONSTRAINT chk_total_marks_positive 
    CHECK (total_marks > 0);

ALTER TABLE test_questions ADD CONSTRAINT chk_marks_positive 
    CHECK (marks > 0);

-- Ensure submitted_at is after started_at
ALTER TABLE test_attempts ADD CONSTRAINT chk_submitted_after_started
    CHECK (submitted_at IS NULL OR submitted_at >= started_at);

-- Ensure passing marks don't exceed total marks
ALTER TABLE tests ADD CONSTRAINT chk_passing_marks_valid
    CHECK (passing_marks IS NULL OR passing_marks <= total_marks);

-- ============================================
-- Note: PostgreSQL-Specific Features
-- ============================================
-- Triggers for updated_at columns are PostgreSQL-specific
-- and not supported in H2. These will be added in a separate
-- PostgreSQL-only migration when deploying to staging/production.
