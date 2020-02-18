-- ------------------------------------------------------------
-- Awards
-- ------------------------------------------------------------

DROP TABLE IF EXISTS awards;
CREATE TABLE awards (
     awardID VARCHAR(36) PRIMARY KEY,
     name VARCHAR(64) NOT NULL,
     code VARCHAR(12) NOT NULL,
     icon VARCHAR(64) NOT NULL,
     description TEXT NULL
);

CREATE UNIQUE INDEX awards_xpk_name ON users (name);
CREATE UNIQUE INDEX awards_xpk_code ON users (code);

INSERT INTO awards (awardID, name, code, icon, description)
VALUES (uuid(), 'Perk-master', 'PERK', 'perk.png', 'You bought a perk');

INSERT INTO awards (awardID, name, code, icon, description)
VALUES (uuid(), 'Winner', 'WINNER', 'checkered_flag.png', 'You won a game with 4 or more players');

-- ------------------------------------------------------------
-- Events
-- ------------------------------------------------------------

DROP TABLE IF EXISTS events;
CREATE TABLE events (
     eventID VARCHAR(36) PRIMARY KEY,
     eventType VARCHAR(64) NOT NULL,
     eventJson TEXT NOT NULL,
     creationTime DATETIME NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- News
-- ------------------------------------------------------------

DROP TABLE IF EXISTS newsSources;
CREATE TABLE newsSources (
     _id VARCHAR(36) NOT NULL PRIMARY KEY,
     name VARCHAR(64) NOT NULL,
     url VARCHAR(255) NOT NULL,
     icon VARCHAR(64) NULL,
     priority INTEGER NOT NULL DEFAULT 0
);

INSERT INTO newsSources (_id, name, url, priority) VALUES ('1', 'CNN Money: Markets', 'http://rss.cnn.com/rss/money_markets.rss', 1) ;
INSERT INTO newsSources (_id, name, url, priority) VALUES ('2', 'CNN Money: Latest News', 'http://rss.cnn.com/rss/money_latest.rss', 2);
INSERT INTO newsSources (_id, name, url, priority) VALUES ('3', 'CBNC News', 'http://www.cnbc.com/id/100003114/device/rss/rss', 3);
INSERT INTO newsSources (_id, name, url, priority) VALUES ('4', 'MarketWatch: Real-time Headlines', 'http://feeds.marketwatch.com/marketwatch/realtimeheadlines/', 4);
INSERT INTO newsSources (_id, name, url, priority) VALUES ('5', 'MarketWatch: Stocks to Watch', 'http://feeds.marketwatch.com/marketwatch/StockstoWatch/', 5);
INSERT INTO newsSources (_id, name, url, priority) VALUES ('6', 'NASDAQ Stocks News', 'http://articlefeeds.nasdaq.com/nasdaq/categories?category=Stocks', 6);

-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------

DROP TABLE IF EXISTS users;
CREATE TABLE users (
     userID VARCHAR(36) PRIMARY KEY,
     name VARCHAR(40) NOT NULL,
     wallet DECIMAL(12,5) NOT NULL,
     creationTime DATETIME NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX users_xpk_name ON users (name);

-- ------------------------------------------------------------
-- Stocks
-- ------------------------------------------------------------

DROP TABLE IF EXISTS stocks;
CREATE TABLE stocks (
    stockID VARCHAR(36) PRIMARY KEY,
    symbol VARCHAR(12) NOT NULL,
    exchange VARCHAR(12) NOT NULL,
    name VARCHAR(255),
    lastSale DECIMAL(12,5),
    closed BIT NOT NULL DEFAULT 0,
    tradeDateTime DATETIME
);

CREATE UNIQUE INDEX stocks_xpk_name ON stocks (symbol);

-- ------------------------------------------------------------
-- Contests
-- ------------------------------------------------------------

DROP TABLE IF EXISTS contests;
CREATE TABLE contests (
     contestID VARCHAR(36) PRIMARY KEY,
     name VARCHAR(128) NOT NULL,
     startingBalance DECIMAL(12,5) NOT NULL,
     creationTime DATETIME NOT NULL DEFAULT now(),
     startTime DATETIME NOT NULL,
     expirationTime DATETIME NOT NULL
);
CREATE UNIQUE INDEX contests_xpk_name ON contests (name);

DROP TABLE IF EXISTS contest_chats;
CREATE TABLE contest_chats (
     messageID VARCHAR(36) PRIMARY KEY,
     contestID VARCHAR(36) NOT NULL,
     playerID VARCHAR(36) NOT NULL,
     message TEXT NOT NULL,
     creationTime DATETIME NOT NULL DEFAULT now()
);

DROP TABLE IF EXISTS contest_players;
CREATE TABLE contest_players (
     playerID VARCHAR(36) PRIMARY KEY DEAULT uuid(),
     userID VARCHAR(36) NOT NULL,
     contestID VARCHAR(36) NOT NULL,
     funds DECIMAL(12,5) NOT NULL,
     joinTime DATETIME NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX contest_players_xpk ON contest_players (contestID, userID);

-- ------------------------------------------------------------
-- Orders
-- ------------------------------------------------------------

DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
     orderID VARCHAR(36) PRIMARY KEY,
     userID VARCHAR(36) NOT NULL,
     symbol VARCHAR(12) NOT NULL,
     exchange VARCHAR(12) NOT NULL,
     orderType VARCHAR(10) NOT NULL,
     price DECIMAL(12,5) NOT NULL,
     priceType VARCHAR(36) NOT NULL,
     quantity INTEGER NOT NULL,
     creationTime DATETIME NOT NULL DEFAULT now(),
     expirationTime DATETIME NOT NULL,
     processedTime DATETIME NULL,
     closed BIT NOT NULL DEFAULT 0,
     fulfilled BIT NOT NULL DEFAULT 0,
     message TEXT NULL
);

DROP TABLE IF EXISTS order_price_types;
CREATE TABLE order_price_types (
    priceTypeID VARCHAR(36) PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    comission DECIMAL(4, 2) NOT NULL
);

CREATE UNIQUE INDEX order_price_types_xpk_name ON order_price_types (name);

INSERT INTO order_price_types ( priceTypeID, name, comission ) VALUES (uuid(), 'LIMIT', 14.99 );
INSERT INTO order_price_types ( priceTypeID, name, comission ) VALUES (uuid(), 'MARKET', 9.99);
INSERT INTO order_price_types ( priceTypeID, name, comission ) VALUES (uuid(), 'MARKET_AT_CLOSE', 6.99);

-- ------------------------------------------------------------
-- Positions
-- ------------------------------------------------------------

DROP TABLE IF EXISTS positions;
CREATE TABLE positions (
     positionID VARCHAR(36) PRIMARY KEY,
     userID VARCHAR(36) NOT NULL,
     orderID VARCHAR(36) NOT NULL,
     symbol VARCHAR(12) NOT NULL,
     exchange VARCHAR(12) NOT NULL,
     price DECIMAL(12,5) NOT NULL,
     quantity INTEGER NOT NULL,
     tradeDateTime DATETIME NOT NULL,
     processedTime DATETIME NOT NULL
);

CREATE UNIQUE INDEX positions_xpk ON positions (orderID);

