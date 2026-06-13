-- Create the RDS IAM-authenticated database user.
--
-- IMPORTANT: this migration runs as the RDS master user (gbo_admin). The
-- gbo_app user authenticates via short-lived IAM tokens, never with a static
-- password. AWS Secrets Manager rotates the master credentials automatically
-- (every 30 days) — no application change is required when rotation occurs.
--
-- See handoff.md §7.3 for full RDS IAM auth setup.

-- Create the application role with no password (IAM auth only).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'gbo_app') THEN
        CREATE ROLE gbo_app WITH LOGIN;
    END IF;
END
$$;

-- Grant the minimum required privileges on existing tables.
GRANT CONNECT ON DATABASE greatest_banking_orchestrator TO gbo_app;
GRANT USAGE  ON SCHEMA public TO gbo_app;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES    IN SCHEMA public TO gbo_app;
GRANT USAGE,  SELECT                  ON ALL SEQUENCES IN SCHEMA public TO gbo_app;

-- Same privileges for any future tables / sequences (created by V3+).
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES    TO gbo_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT                   ON SEQUENCES TO gbo_app;

-- Enable RDS IAM authentication for this role. The "rds_iam" predefined role
-- is created automatically by Aurora PostgreSQL.
GRANT rds_iam TO gbo_app;
