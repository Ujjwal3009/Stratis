-- Seed core subjects for testing
INSERT INTO subjects (name, description)
SELECT 'History', 'Indian and World History' WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'History');
INSERT INTO subjects (name, description)
SELECT 'Polity', 'Indian Constitution and Governance' WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Polity');
INSERT INTO subjects (name, description)
SELECT 'Geography', 'Physical and Indian Geography' WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Geography');
INSERT INTO subjects (name, description)
SELECT 'Economy', 'Indian Economic Development' WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Economy');
INSERT INTO subjects (name, description)
SELECT 'Science', 'General Science and Technology' WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Science');
