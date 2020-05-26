USE shocktrade;

-- ------------------------------------------------------------
-- Traffic table
-- ------------------------------------------------------------

DROP TABLE IF EXISTS traffic;
CREATE TABLE traffic (
    uid BIGINT AUTO_INCREMENT PRIMARY KEY,
    method VARCHAR(255),
    path VARCHAR(255),
    query TEXT,
    statusCode SMALLINT,
    statusMessage TEXT,
    responseTimeMillis INTEGER,
    requestTime DATETIME NOT NULL,
    creationTime DATETIME NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- Virtual Machine table
-- ------------------------------------------------------------

DROP TABLE IF EXISTS eventsource;
CREATE TABLE eventsource (
    uid BIGINT AUTO_INCREMENT PRIMARY KEY,
    command TEXT NOT NULL,
    type VARCHAR(64) NOT NULL,
    contestID CHAR(36),
    portfolioID CHAR(36),
    positionID CHAR(36),
    userID CHAR(36),
    orderID CHAR(36),
    symbol VARCHAR(20),
    exchange VARCHAR(20),
    orderType VARCHAR(20),
    priceType VARCHAR(20),
    quantity DOUBLE,
    negotiatedPrice DOUBLE,
    xp DOUBLE,
    response TEXT,
    responseTimeMillis INTEGER,
    failed TINYINT NOT NULL DEFAULT 0,
    creationTime DATETIME NOT NULL DEFAULT now()
);

CREATE INDEX eventsource_xp ON eventsource (contestID, portfolioID, userID );