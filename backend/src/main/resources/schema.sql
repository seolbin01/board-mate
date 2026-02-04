-- Add reminder_sent column with default value (safe for existing rows)
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS reminder_sent BOOLEAN DEFAULT false;
UPDATE rooms SET reminder_sent = false WHERE reminder_sent IS NULL;

-- Enable pgvector extension for RAG embeddings
CREATE EXTENSION IF NOT EXISTS vector;

-- Drop old table if dimension changed (Gemini uses 768, OpenAI used 1536)
DROP TABLE IF EXISTS game_embeddings;

-- Create game_embeddings table with Gemini dimension (768)
CREATE TABLE IF NOT EXISTS game_embeddings (
    bgg_id BIGINT PRIMARY KEY,
    content TEXT NOT NULL,
    embedding vector(768)
);

-- Create index for similarity search (need enough rows first, so skip if empty)
-- CREATE INDEX IF NOT EXISTS idx_game_embeddings_vector ON game_embeddings USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
