-- Modify existing tests table
ALTER TABLE tests ADD COLUMN IF NOT EXISTS subject_id BIGINT REFERENCES subjects(id);
ALTER TABLE tests ADD COLUMN IF NOT EXISTS topic_id BIGINT REFERENCES topics(id);
ALTER TABLE tests ADD COLUMN IF NOT EXISTS target_difficulty VARCHAR(20); -- EASY, MEDIUM, HARD
ALTER TABLE tests ADD COLUMN IF NOT EXISTS total_questions INT;

-- Modify duration_minutes to have a default (if not already there)
ALTER TABLE tests ALTER COLUMN duration_minutes SET DEFAULT 30;

-- Modify existing test_attempts table
ALTER TABLE test_attempts ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;
-- V1 has score as DECIMAL(5,2), our entity expects Integer for now based on implementation.
-- I'll keep it as is or change it. Let's keep the DB as DECIMAL for precision if needed later, 
-- but add the status column if it differs. V1 has status VARCHAR(50).
-- V1 has submitted_at, our entity used completed_at. I'll just add completed_at.

-- User answers (V1 has user_answers, let's see)
-- V1 has user_answers, so we don't need to create it if it already exists.
-- But we need test_questions_mapping for our JPA relationship
CREATE TABLE IF NOT EXISTS test_questions_mapping (
    test_id BIGINT REFERENCES tests(id) ON DELETE CASCADE,
    question_id BIGINT REFERENCES questions(id),
    marks INT DEFAULT 1,
    question_order INT,
    PRIMARY KEY (test_id, question_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_tests_subject_id ON tests(subject_id);
CREATE INDEX IF NOT EXISTS idx_tests_topic_id ON tests(topic_id);
CREATE INDEX IF NOT EXISTS idx_test_attempts_user_check ON test_attempts(user_id);
