USE shocktrade;

-- ------------------------------------------------------------
-- Contest tables
-- ------------------------------------------------------------

DROP TABLE IF EXISTS contests;
CREATE TABLE contests (
     contestID CHAR(36) PRIMARY KEY,
     name VARCHAR(128) NOT NULL,
     hostUserID CHAR(36) NOT NULL,
     status VARCHAR(12) NOT NULL DEFAULT 'ACTIVE',
     startingBalance DECIMAL(12,5) NOT NULL,
     friendsOnly BIT NOT NULL DEFAULT 0,
     invitationOnly BIT NOT NULL DEFAULT 0,
     levelCap BIT NOT NULL DEFAULT 0,
     perksAllowed BIT NOT NULL DEFAULT 1,
     robotsAllowed BIT NOT NULL DEFAULT 1,
     creationTime DATETIME NOT NULL DEFAULT now(),
     startTime DATETIME NOT NULL DEFAULT now(),
     expirationTime DATETIME NULL
);

DROP VIEW IF EXISTS contest_rankings;
CREATE VIEW contest_rankings AS
SELECT
    C.*,
    P.portfolioID,
    U.userID,
    U.username,
    P.funds + SUM(S.lastTrade * PS.quantity) AS totalEquity,
    (100 * ((P.funds + SUM(S.lastTrade * PS.quantity)) / C.startingBalance)) - 100 AS gainLoss
FROM contests C
LEFT JOIN portfolios P ON P.contestID = C.contestID
LEFT JOIN users U ON U.userID = P.userID
LEFT JOIN positions PS ON PS.portfolioID = P.portfolioID
LEFT JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
GROUP BY C.contestID, C.name, P.portfolioID, U.userID, U.username
ORDER BY totalEquity DESC;

-- ------------------------------------------------------------
-- Chat tables
-- ------------------------------------------------------------

DROP TABLE IF EXISTS contest_chats;
CREATE TABLE contest_chats (
     messageID CHAR(36) PRIMARY KEY,
     contestID CHAR(36) NOT NULL,
     portfolioID CHAR(36) NOT NULL,
     message TEXT NOT NULL,
     creationTime DATETIME NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- Portfolio tables
-- ------------------------------------------------------------

DROP TABLE IF EXISTS portfolios;
CREATE TABLE portfolios (
     portfolioID CHAR(36) PRIMARY KEY,
     userID CHAR(36) NOT NULL,
     contestID CHAR(36) NOT NULL,
     funds DECIMAL(12,5) NOT NULL,
     joinTime DATETIME NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX portfolios_xpk ON portfolios (contestID, userID);

-- ------------------------------------------------------------
-- Orders
-- ------------------------------------------------------------

DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
     orderID CHAR(36) PRIMARY KEY,
     portfolioID CHAR(36) NOT NULL,
     symbol VARCHAR(12) NOT NULL,
     exchange VARCHAR(12) NOT NULL,
     orderType VARCHAR(10) NOT NULL,
     price DECIMAL(12,5) NOT NULL,
     priceType VARCHAR(36) NOT NULL,
     quantity INTEGER NOT NULL,
     creationTime DATETIME NOT NULL DEFAULT now(),
     expirationTime DATETIME NULL,
     processedTime DATETIME NULL,
     closed BIT NOT NULL DEFAULT 0,
     fulfilled BIT NOT NULL DEFAULT 0,
     message TEXT NULL
);

DROP TABLE IF EXISTS order_price_types;
CREATE TABLE order_price_types (
    priceTypeID CHAR(36) PRIMARY KEY,
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
     positionID CHAR(36) PRIMARY KEY,
     portfolioID CHAR(36) NOT NULL,
     orderID CHAR(36) NOT NULL,
     symbol VARCHAR(12) NOT NULL,
     exchange VARCHAR(12) NOT NULL,
     price DECIMAL(12,5) NOT NULL,
     quantity INTEGER NOT NULL,
     tradeDateTime DATETIME NOT NULL,
     creationTime DATETIME NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX positions_xpk ON positions (orderID);


