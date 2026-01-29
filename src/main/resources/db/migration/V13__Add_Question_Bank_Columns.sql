-- Add new columns to questions table
ALTER TABLE questions ADD COLUMN IF NOT EXISTS cognitive_level VARCHAR(50);
ALTER TABLE questions ADD COLUMN IF NOT EXISTS options JSONB;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS correct_answer TEXT;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS explanation_json JSONB;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS exam_year INTEGER;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS paper VARCHAR(100);
ALTER TABLE questions ADD COLUMN IF NOT EXISTS passage TEXT;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS image_url TEXT;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS normalized_hash VARCHAR(500);
ALTER TABLE questions ADD COLUMN IF NOT EXISTS created_source VARCHAR(50);
ALTER TABLE questions ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_questions_subject_difficulty ON questions(subject_id, difficulty_level);
CREATE INDEX IF NOT EXISTS idx_questions_topic_difficulty ON questions(topic_id, difficulty_level);
CREATE INDEX IF NOT EXISTS idx_questions_created_source ON questions(created_source);
CREATE INDEX IF NOT EXISTS idx_questions_normalized_hash ON questions(normalized_hash);

-- Enforce unique constraint on normalized_hash where it is not null
CREATE UNIQUE INDEX IF NOT EXISTS idx_questions_normalized_hash_unique ON questions(normalized_hash);
