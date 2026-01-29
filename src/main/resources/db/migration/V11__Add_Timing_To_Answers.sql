-- Migration: Add timing data to user answers for advanced analysis
-- Version: 11

ALTER TABLE user_answers 
ADD COLUMN time_spent_seconds INTEGER DEFAULT 0;
