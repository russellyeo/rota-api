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
