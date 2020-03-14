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

-- ------------------------------------------------------------
-- Perks
-- ------------------------------------------------------------

DROP TABLE IF EXISTS perks;
CREATE TABLE perks (
    perkID INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(40) NOT NULL,
    code VARCHAR(20) NOT NULL,
    cost DECIMAL(9,2) NOT NULL,
    description TEXT NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX perk_xp_code ON perks (code);
CREATE UNIQUE INDEX perk_xp_name ON perks (name);

INSERT INTO perks (name, code, cost, description)
VALUES
    ("Purchase Eminent", "PRCHEMNT", 500, "Gives the player the ability to create SELL orders for securities not yet owned"),
    ("Perfect Timing", "PRFCTIMG", 500, "Gives the player the ability to create BUY orders for more than cash currently available"),
    ("Compounded Daily", "CMPDDALY", 1000, "Gives the player the ability to earn interest on cash not currently invested"),
    ("Fee Waiver", "FEEWAIVR", 2500, "Reduces the commissions the player pays for buying or selling securities"),
    ("Rational People think at the Margin", "MARGIN", 2500, "Gives the player the ability to use margin accounts"),
    ("Savings and Loans", "SAVGLOAN", 5000, "Gives the player the ability to borrow money"),
    ("Loan Shark", "LOANSHRK", 5000, "Gives the player the ability to loan other players money at any interest rate"),
    ("The Feeling's Mutual", "MUTFUNDS", 5000, "Gives the player the ability to create and use mutual funds"),
    ("Risk Management",  "RISKMGMT", 5000, "Gives the player the ability to trade options");
