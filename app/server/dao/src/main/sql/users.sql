-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------

DROP TABLE IF EXISTS users;
CREATE TABLE users (
    userID CHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(40) NOT NULL,
    email VARCHAR(255) NULL,
    password TEXT NULL,
    wallet DECIMAL(17,5) NOT NULL,
    creationTime DATETIME NOT NULL DEFAULT now(),
    lastModifiedTime DATETIME NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX users_xpk_name ON users (username);

DROP TABLE IF EXISTS user_sessions;
CREATE TABLE user_sessions (
    sessionID CHAR(36) NOT NULL PRIMARY KEY,
    userID CHAR(36) NOT NULL,
    authCode CHAR(16) NOT NULL,
    loginTime DATETIME NOT NULL DEFAULT now(),
    logoutTime DATETIME NOT NULL DEFAULT now()
);