USE shocktrade;

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

