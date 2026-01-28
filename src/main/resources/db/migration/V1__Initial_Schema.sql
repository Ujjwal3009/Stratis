-- Initial Schema for UPSC AI Platform
-- Version: 1
-- Description: Creates core tables for users, questions, tests, and results

-- ============================================
-- Users and Authentication
-- ============================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255),
    oauth_provider VARCHAR(50),
    oauth_provider_id VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_oauth ON users(oauth_provider, oauth_provider_id);

-- ============================================
-- Questions
-- ============================================

CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    difficulty_level VARCHAR(50) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    topic VARCHAR(255),
    explanation TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_questions_subject ON questions(subject);
CREATE INDEX idx_questions_difficulty ON questions(difficulty_level);
CREATE INDEX idx_questions_type ON questions(question_type);
CREATE INDEX idx_questions_created_by ON questions(created_by);

-- ============================================
-- Question Options (for MCQs)
-- ============================================

CREATE TABLE question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT false,
    option_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_question_options_question_id ON question_options(question_id);

-- ============================================
-- Tests
-- ============================================

CREATE TABLE tests (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    test_type VARCHAR(50) NOT NULL,
    duration_minutes INT NOT NULL,
    total_marks INT NOT NULL,
    passing_marks INT,
    is_published BOOLEAN NOT NULL DEFAULT false,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tests_created_by ON tests(created_by);
CREATE INDEX idx_tests_is_published ON tests(is_published);
CREATE INDEX idx_tests_type ON tests(test_type);

-- ============================================
-- Test Questions (Many-to-Many)
-- ============================================

CREATE TABLE test_questions (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    marks INT NOT NULL,
    question_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(test_id, question_id)
);

CREATE INDEX idx_test_questions_test_id ON test_questions(test_id);
CREATE INDEX idx_test_questions_question_id ON test_questions(question_id);

-- ============================================
-- Test Attempts
-- ============================================

CREATE TABLE test_attempts (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT NOT NULL REFERENCES tests(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP,
    score DECIMAL(5,2),
    total_marks INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_test_attempts_user_id ON test_attempts(user_id);
CREATE INDEX idx_test_attempts_test_id ON test_attempts(test_id);
CREATE INDEX idx_test_attempts_status ON test_attempts(status);

-- ============================================
-- User Answers
-- ============================================

CREATE TABLE user_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL REFERENCES test_attempts(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id),
    selected_option_id BIGINT REFERENCES question_options(id),
    answer_text TEXT,
    is_correct BOOLEAN,
    marks_awarded DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(attempt_id, question_id)
);

CREATE INDEX idx_user_answers_attempt_id ON user_answers(attempt_id);
CREATE INDEX idx_user_answers_question_id ON user_answers(question_id);
