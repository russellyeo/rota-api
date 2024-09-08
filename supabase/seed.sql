-- Seed Data for USERS table
INSERT INTO "USERS" ("NAME") VALUES ('Russell Yeo');
INSERT INTO "USERS" ("NAME") VALUES ('Jane Doe');
INSERT INTO "USERS" ("NAME") VALUES ('John Smith');

-- Seed Data for ROTAS table
INSERT INTO "ROTAS" ("NAME", "DESCRIPTION", "ASSIGNED_USER_ID") VALUES ('standup', 'Daily team standup', 1);
INSERT INTO "ROTAS" ("NAME", "DESCRIPTION", "ASSIGNED_USER_ID") VALUES ('retrospective', 'Monthly retrospective', 2);
INSERT INTO "ROTAS" ("NAME", "DESCRIPTION", "ASSIGNED_USER_ID") VALUES ('release-captain', 'The weekly release cycle', NULL);

-- Seed Data for ROTA_USERS table
INSERT INTO "ROTA_USERS" ("ROTA_NAME", "USER_ID") VALUES ('standup', 1);
INSERT INTO "ROTA_USERS" ("ROTA_NAME", "USER_ID") VALUES ('standup', 2);
INSERT INTO "ROTA_USERS" ("ROTA_NAME", "USER_ID") VALUES ('standup', 3);
INSERT INTO "ROTA_USERS" ("ROTA_NAME", "USER_ID") VALUES ('retrospective', 1);
INSERT INTO "ROTA_USERS" ("ROTA_NAME", "USER_ID") VALUES ('retrospective', 2);
INSERT INTO "ROTA_USERS" ("ROTA_NAME", "USER_ID") VALUES ('retrospective', 3);
INSERT INTO "ROTA_USERS" ("ROTA_NAME", "USER_ID") VALUES ('release-captain', 2);
INSERT INTO "ROTA_USERS" ("ROTA_NAME", "USER_ID") VALUES ('release-captain', 3);

-- Seed Data for slack_teams table
INSERT INTO "slack_teams" ("id", "team", "is_enterprise") VALUES 
('TUK5AALTR', 'Team Alpha', false),
('TUK3RRETW', 'Team Beta', true);

-- Seed Data for slack_bots table
INSERT INTO "slack_bots" ("id", "token", "user_id") VALUES 
('B01J4F9RJ9Q', 'xoxb-bot-token-1', 'UUGHRRSG4'),
('B01G5F2RJ7W', 'xoxb-bot-token-2', 'UUYDLPTK5');

-- Seed Data for slack_users table
INSERT INTO "slack_users" ("id", "token") VALUES
('UUGHRRSG4', 'xoxp-user-token-1'),
('UUYDLPTK5', 'xoxp-user-token-2');

-- Seed Data for slack_installations table
INSERT INTO "slack_installations" ("team_id", "user_id", "token_type", "is_enterprise_install", "app_id", "auth_version", "bot_id") VALUES 
('TUK5AALTR', 'UUGHRRSG4', 'bot', false, 'A01J9G4DJ9Q', 'v2', 'B01J4F9RJ9Q'),
('TUK3RRETW', 'UUYDLPTK5', 'bot', true, 'A01J9R8QT8R', 'v2', 'B01G5F2RJ7W');