-- Seed extensive list of UPSC subjects
-- Using INSERT ... SELECT ... WHERE NOT EXISTS for compatibility with H2 and PostgreSQL (since H2 is not in Postgres mode)

INSERT INTO subjects (name, description, created_at)
SELECT 'Ancient History', 'Indus Valley to Guptas & Post-Gupta Era', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Ancient History');

INSERT INTO subjects (name, description, created_at)
SELECT 'Medieval History', 'Delhi Sultanate, Mughals, Vijaynagar & Bhakti/Sufi', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Medieval History');

INSERT INTO subjects (name, description, created_at)
SELECT 'Modern History', 'British Conquest to Independence (1857-1947)', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Modern History');

INSERT INTO subjects (name, description, created_at)
SELECT 'Art & Culture', 'Architecture, Music, Dance & Literature of India', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Art & Culture');

INSERT INTO subjects (name, description, created_at)
SELECT 'Physical Geography', 'Geomorphology, Climatology & Oceanography', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Physical Geography');

INSERT INTO subjects (name, description, created_at)
SELECT 'Indian Geography', 'Physiography, Drainage, Climate & Agriculture', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Indian Geography');

INSERT INTO subjects (name, description, created_at)
SELECT 'World Geography', 'Continents, Resources & Economic Geography', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'World Geography');

INSERT INTO subjects (name, description, created_at)
SELECT 'Polity', 'Indian Constitution, Parliament, Judiciary & Local Govt', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Polity');

INSERT INTO subjects (name, description, created_at)
SELECT 'Governance', 'Rights, Policies & Constitutional Bodies', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Governance');

INSERT INTO subjects (name, description, created_at)
SELECT 'Economy', 'Banking, Budget, Planning & Agriculture', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Economy');

INSERT INTO subjects (name, description, created_at)
SELECT 'Environment', 'Ecology, Biodiversity, Climate Change & Pollution', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Environment');

INSERT INTO subjects (name, description, created_at)
SELECT 'Science & Tech', 'Space, Defense, Biotech, & Emerging Tech', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Science & Tech');

INSERT INTO subjects (name, description, created_at)
SELECT 'International Relations', 'India''s Foreign Policy & Global Institutions', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'International Relations');

INSERT INTO subjects (name, description, created_at)
SELECT 'Current Affairs', 'National & International Events of Importance', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE name = 'Current Affairs');
