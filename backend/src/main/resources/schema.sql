-- Add reminder_sent column if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'rooms' AND column_name = 'reminder_sent'
    ) THEN
        ALTER TABLE rooms ADD COLUMN reminder_sent BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;
