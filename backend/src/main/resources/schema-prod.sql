-- Add reminder_sent column with default value (safe for existing rows)
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS reminder_sent BOOLEAN DEFAULT false;
UPDATE rooms SET reminder_sent = false WHERE reminder_sent IS NULL;

-- Note: pgvector extension is not available on Railway PostgreSQL
-- RAG features will use Gemini's knowledge only (no vector search)
