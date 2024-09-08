-- Create tables for storing slack installations

-- Create slack_teams table first as it's referenced by other tables
CREATE TABLE slack_teams (
    id VARCHAR(255) PRIMARY KEY,
    team VARCHAR(255) NOT NULL,
    is_enterprise BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create slack_bots table
CREATE TABLE slack_bots (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create slack_users table
CREATE TABLE slack_users (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create slack_installations table with foreign key references
CREATE TABLE slack_installations (
  team_id VARCHAR(255) PRIMARY KEY, 
  user_id VARCHAR(255) NOT NULL,
  token_type VARCHAR(50) NOT NULL,
  is_enterprise_install BOOLEAN NOT NULL,
  app_id VARCHAR(255) NOT NULL,
  auth_version VARCHAR(50) NOT NULL,
  bot_id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (team_id) REFERENCES slack_teams(id),
  FOREIGN KEY (bot_id) REFERENCES slack_bots(id),
  FOREIGN KEY (user_id) REFERENCES slack_users(id)
);

-- Create slack_bot_scopes table
CREATE TABLE slack_bot_scopes (
    bot_id VARCHAR(255) NOT NULL,
    scope VARCHAR(255) NOT NULL,
    PRIMARY KEY (bot_id, scope),
    FOREIGN KEY (bot_id) REFERENCES slack_bots(id) ON DELETE CASCADE
);

-- Create slack_user_scopes table
CREATE TABLE slack_user_scopes (
    user_id VARCHAR(255) NOT NULL,
    scope VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, scope),
    FOREIGN KEY (user_id) REFERENCES slack_users(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_slack_bot_scopes_bot_id ON slack_bot_scopes(bot_id);
CREATE INDEX idx_slack_user_scopes_user_id ON slack_user_scopes(user_id);
CREATE INDEX idx_slack_installations_bot_id ON slack_installations(bot_id);
CREATE INDEX idx_slack_installations_user_id ON slack_installations(user_id);

-- Create function for updating 'updated_at' column
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updating 'updated_at' column
CREATE TRIGGER update_slack_installations_modtime
BEFORE UPDATE ON slack_installations
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_slack_bots_modtime
BEFORE UPDATE ON slack_bots
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_slack_teams_modtime
BEFORE UPDATE ON slack_teams
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_slack_users_modtime
BEFORE UPDATE ON slack_users
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- Add comments to tables for better documentation
COMMENT ON TABLE slack_installations IS 'Stores Slack app installation data';
COMMENT ON TABLE slack_bots IS 'Stores Slack bot information';
COMMENT ON TABLE slack_bot_scopes IS 'Stores scopes for Slack bots';
COMMENT ON TABLE slack_teams IS 'Stores Slack team information';
COMMENT ON TABLE slack_users IS 'Stores Slack user information';
COMMENT ON TABLE slack_user_scopes IS 'Stores scopes for Slack users';