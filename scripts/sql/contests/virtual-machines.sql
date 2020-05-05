USE shocktrade;

-- ------------------------------------------------------------
-- Virtual Machine table
-- ------------------------------------------------------------

DROP TABLE IF EXISTS eventsource;
CREATE TABLE eventsource (
    uid BIGINT AUTO_INCREMENT PRIMARY KEY,
    command TEXT NOT NULL,
    type VARCHAR(64) NOT NULL,
    contestID CHAR(36) NULL,
    portfolioID CHAR(36) NULL,
    positionID CHAR(36) NULL,
    userID CHAR(36) NULL,
    orderID CHAR(36) NULL,
    symbol CHAR(20) NULL,
    exchange CHAR(20) NULL,
    response TEXT NULL,
    responseTimeMillis INTEGER NULL,
    creationTime DATETIME NOT NULL DEFAULT now()
);