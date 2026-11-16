ALTER TABLE export_logs
    ADD COLUMN content_snapshot JSON NULL AFTER status,
    ADD COLUMN resume_version INT NULL AFTER content_snapshot;
