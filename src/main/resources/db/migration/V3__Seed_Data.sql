-- Seed Data for Development and Testing
-- Version: 3
-- Description: Insert default admin user and sample data

-- ============================================
-- Default Admin User
-- ============================================

-- Insert admin user (using INSERT ON CONFLICT for PostgreSQL/H2 compatibility)
-- Password: admin123 (bcrypt hashed)
-- Note: In production, this should be changed immediately
INSERT INTO users (email, username, password_hash, full_name, role, is_active, created_at, updated_at)
VALUES (
    'admin@upsc-ai.com',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'System Administrator',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- ============================================
-- Sample Test Users
-- ============================================

-- Test user 1 (password: test123)
INSERT INTO users (email, username, password_hash, full_name, role, is_active, created_at, updated_at)
VALUES (
    'test@upsc-ai.com',
    'testuser',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test User',
    'USER',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- ============================================
-- Sample Questions (Optional - for development)
-- ============================================

-- Sample MCQ Question 1
INSERT INTO questions (question_text, question_type, difficulty_level, subject, topic, explanation, created_by)
VALUES (
    'Who was the first President of India?',
    'MCQ',
    'EASY',
    'History',
    'Indian Independence',
    'Dr. Rajendra Prasad was the first President of India, serving from 1950 to 1962.',
    (SELECT id FROM users WHERE email = 'admin@upsc-ai.com')
);

-- Options for Question 1
INSERT INTO question_options (question_id, option_text, is_correct, option_order)
VALUES 
    ((SELECT id FROM questions WHERE question_text LIKE 'Who was the first President%'), 'Dr. Rajendra Prasad', true, 1),
    ((SELECT id FROM questions WHERE question_text LIKE 'Who was the first President%'), 'Jawaharlal Nehru', false, 2),
    ((SELECT id FROM questions WHERE question_text LIKE 'Who was the first President%'), 'Sardar Vallabhbhai Patel', false, 3),
    ((SELECT id FROM questions WHERE question_text LIKE 'Who was the first President%'), 'Dr. B.R. Ambedkar', false, 4);

-- Sample MCQ Question 2
INSERT INTO questions (question_text, question_type, difficulty_level, subject, topic, explanation, created_by)
VALUES (
    'The Indian Constitution came into effect on which date?',
    'MCQ',
    'MEDIUM',
    'Polity',
    'Constitution',
    'The Constitution of India came into effect on 26th January 1950, which is celebrated as Republic Day.',
    (SELECT id FROM users WHERE email = 'admin@upsc-ai.com')
);

-- Options for Question 2
INSERT INTO question_options (question_id, option_text, is_correct, option_order)
VALUES 
    ((SELECT id FROM questions WHERE question_text LIKE 'The Indian Constitution came into effect%'), '26 January 1950', true, 1),
    ((SELECT id FROM questions WHERE question_text LIKE 'The Indian Constitution came into effect%'), '15 August 1947', false, 2),
    ((SELECT id FROM questions WHERE question_text LIKE 'The Indian Constitution came into effect%'), '26 November 1949', false, 3),
    ((SELECT id FROM questions WHERE question_text LIKE 'The Indian Constitution came into effect%'), '2 October 1950', false, 4);

-- ============================================
-- Sample Test
-- ============================================

INSERT INTO tests (title, description, test_type, duration_minutes, total_marks, passing_marks, is_published, created_by)
VALUES (
    'Sample UPSC Prelims Test',
    'A sample test covering basic questions on Indian History and Polity',
    'PRELIMS',
    30,
    10,
    6,
    true,
    (SELECT id FROM users WHERE email = 'admin@upsc-ai.com')
);

-- Link questions to test
INSERT INTO test_questions (test_id, question_id, marks, question_order)
SELECT 
    (SELECT id FROM tests WHERE title = 'Sample UPSC Prelims Test'),
    id,
    5,
    ROW_NUMBER() OVER (ORDER BY id)
FROM questions
WHERE created_by = (SELECT id FROM users WHERE email = 'admin@upsc-ai.com');
