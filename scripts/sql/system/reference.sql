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

DROP TABLE IF EXISTS rss_feeds;
CREATE TABLE rss_feeds (
     rssFeedID VARCHAR(36) NOT NULL PRIMARY KEY,
     name VARCHAR(64) NOT NULL,
     url VARCHAR(255) NOT NULL,
     icon VARCHAR(64) NULL,
     priority INTEGER NOT NULL DEFAULT 0
);

INSERT INTO rss_feeds (rssFeedID, name, url, priority) VALUES (uuid(), 'CNN News.com', 'http://rss.cnn.com/rss/cnn_topstories.rss', 1) ;
INSERT INTO rss_feeds (rssFeedID, name, url, priority) VALUES (uuid(), 'CNN Money: Markets', 'http://rss.cnn.com/rss/money_markets.rss', 1) ;
INSERT INTO rss_feeds (rssFeedID, name, url, priority) VALUES (uuid(), 'CNN Money: Latest News', 'http://rss.cnn.com/rss/money_latest.rss', 2);
INSERT INTO rss_feeds (rssFeedID, name, url, priority) VALUES (uuid(), 'CBNC News', 'http://www.cnbc.com/id/100003114/device/rss/rss', 3);
INSERT INTO rss_feeds (rssFeedID, name, url, priority) VALUES (uuid(), 'MarketWatch: Real-time Headlines', 'http://feeds.marketwatch.com/marketwatch/realtimeheadlines/', 4);
INSERT INTO rss_feeds (rssFeedID, name, url, priority) VALUES (uuid(), 'MarketWatch: Stocks to Watch', 'http://feeds.marketwatch.com/marketwatch/StockstoWatch/', 5);
INSERT INTO rss_feeds (rssFeedID, name, url, priority) VALUES (uuid(), 'NASDAQ Stocks News', 'http://articlefeeds.nasdaq.com/nasdaq/categories?category=Stocks', 6);
