-- Subjects table
CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Topics table
CREATE TABLE IF NOT EXISTS topics (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT REFERENCES subjects(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(subject_id, name)
);

-- Modify Questions table (already exists in V1)
-- Add new columns for structured subject/topic and source tracking
ALTER TABLE questions ADD COLUMN IF NOT EXISTS subject_id BIGINT REFERENCES subjects(id);
ALTER TABLE questions ADD COLUMN IF NOT EXISTS topic_id BIGINT REFERENCES topics(id);
ALTER TABLE questions ADD COLUMN IF NOT EXISTS source_pdf_id BIGINT REFERENCES pdf_documents(id);
ALTER TABLE questions ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT false;

-- Allow legacy string columns to be null
ALTER TABLE questions ALTER COLUMN subject DROP NOT NULL;

-- Indexes for new columns
CREATE INDEX IF NOT EXISTS idx_questions_subject_id ON questions(subject_id);
CREATE INDEX IF NOT EXISTS idx_questions_topic_id ON questions(topic_id);
CREATE INDEX IF NOT EXISTS idx_questions_source_pdf ON questions(source_pdf_id);

-- Question Options (already exists in V1, no structural change needed)
-- But we can add the index if not already present with a clear name
CREATE INDEX IF NOT EXISTS idx_question_options_q_item ON question_options(question_id);
