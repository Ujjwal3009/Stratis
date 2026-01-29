-- Create user_question_attempts table
CREATE TABLE IF NOT EXISTS user_question_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    question_id BIGINT NOT NULL REFERENCES questions(id),
    test_attempt_id BIGINT REFERENCES test_attempts(id),
    selected_option TEXT,
    first_selected_option TEXT,
    option_change_count INTEGER DEFAULT 0,
    time_taken_seconds INTEGER,
    is_correct BOOLEAN,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_uqa_user_question ON user_question_attempts(user_id, question_id);
CREATE INDEX IF NOT EXISTS idx_uqa_user_attempted_at ON user_question_attempts(user_id, attempted_at);
CREATE INDEX IF NOT EXISTS idx_uqa_test_attempt ON user_question_attempts(test_attempt_id);

-- Create user_test_metrics table
CREATE TABLE IF NOT EXISTS user_test_metrics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    test_attempt_id BIGINT NOT NULL REFERENCES test_attempts(id) UNIQUE,
    accuracy_percentage DECIMAL(5,2),
    attempt_ratio DECIMAL(5,2),
    negative_marks DECIMAL(5,2),
    first_instinct_accuracy DECIMAL(5,2),
    elimination_efficiency DECIMAL(5,2),
    impulsive_error_count INTEGER,
    overthinking_error_count INTEGER,
    guess_probability DECIMAL(5,2),
    cognitive_breakdown JSONB,
    risk_appetite_score DECIMAL(5,2),
    fatigue_curve JSONB,
    confidence_index DECIMAL(5,2),
    consistency_index DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_utm_user_id ON user_test_metrics(user_id);
