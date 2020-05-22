USE shocktrade;

-- ------------------------------------------------------------
-- Virtual Machine table
-- ------------------------------------------------------------

DROP TABLE IF EXISTS dailyLimits;
CREATE TABLE dailyLimits (
    uid BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20),
    exchange VARCHAR(20),
    quantity DOUBLE NOT NULL DEFAULT 0,
    creationTime DATETIME NOT NULL DEFAULT now()
);