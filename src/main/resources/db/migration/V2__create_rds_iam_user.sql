-- Create the RDS IAM-authenticated database user.
--
-- IMPORTANT: this migration runs as the RDS master user (pismo_admin). The
-- pismo_app user authenticates via short-lived IAM tokens, never with a static
-- password. AWS Secrets Manager rotates the master credentials automatically
-- (every 30 days) — no application change is required when rotation occurs.
--
-- See handoff.md §7.3 for full RDS IAM auth setup.

-- Create the application role with no password (IAM auth only).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'pismo_app') THEN
        CREATE ROLE pismo_app WITH LOGIN;
    END IF;
END
$$;

-- Grant the minimum required privileges on existing tables.
GRANT CONNECT ON DATABASE pismo_challenge TO pismo_app;
GRANT USAGE  ON SCHEMA public TO pismo_app;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES    IN SCHEMA public TO pismo_app;
GRANT USAGE,  SELECT                  ON ALL SEQUENCES IN SCHEMA public TO pismo_app;

-- Same privileges for any future tables / sequences (created by V3+).
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES    TO pismo_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT                   ON SEQUENCES TO pismo_app;

-- Enable RDS IAM authentication for this role. The "rds_iam" predefined role
-- is created automatically by Aurora PostgreSQL.
GRANT rds_iam TO pismo_app;
