CREATE TABLE user_token_usage (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    total_tokens INTEGER NOT NULL,
    prompt_tokens INTEGER,
    completion_tokens INTEGER,
    feature_area VARCHAR(100), -- e.g., 'TEST_GENERATION', 'ANALYSIS'
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_token_usage_user ON user_token_usage(user_id);
CREATE INDEX idx_token_usage_date ON user_token_usage(used_at);
