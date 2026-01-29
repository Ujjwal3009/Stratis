ALTER TABLE user_question_attempts ADD COLUMN hover_count INTEGER DEFAULT 0;
ALTER TABLE user_question_attempts ADD COLUMN eliminated_option_ids TEXT;
