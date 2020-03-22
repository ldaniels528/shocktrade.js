USE shocktrade;

-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------

DROP TABLE IF EXISTS users;
CREATE TABLE users (
    userID CHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(40) NOT NULL,
    email VARCHAR(255) NULL,
    password TEXT NULL,
    totalXP INTEGER NOT NULL DEFAULT 0,
    wallet DECIMAL(17,5) NOT NULL,
    creationTime DATETIME NOT NULL DEFAULT now(),
    lastModifiedTime DATETIME NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX users_xpk_name ON users (username);

-- ------------------------------------------------------------
-- User Icons
-- ------------------------------------------------------------

DROP TABLE IF EXISTS user_icons;
CREATE TABLE user_icons (
    userID CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(40) NOT NULL,
    mime VARCHAR(40) NOT NULL,
    image BLOB NOT NULL
);

-- ------------------------------------------------------------
-- User Sessions
-- ------------------------------------------------------------

DROP TABLE IF EXISTS user_sessions;
CREATE TABLE user_sessions (
    sessionID CHAR(36) NOT NULL PRIMARY KEY,
    userID CHAR(36) NOT NULL,
    authCode CHAR(16) NOT NULL,
    loginTime DATETIME NOT NULL DEFAULT now(),
    logoutTime DATETIME NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- Favorite & Recent Symbols
-- ------------------------------------------------------------

DROP TABLE IF EXISTS favorite_symbols;
CREATE TABLE favorite_symbols (
    userID CHAR(36) NOT NULL,
    symbol VARCHAR(12) NOT NULL,
    creationTime DATETIME NOT NULL DEFAULT now(),
    PRIMARY KEY(userID, symbol)
);

DROP TABLE IF EXISTS recent_symbols;
CREATE TABLE recent_symbols (
    userID CHAR(36) NOT NULL,
    symbol VARCHAR(12) NOT NULL,
    creationTime DATETIME NOT NULL DEFAULT now(),
    PRIMARY KEY(userID, symbol)
);
