USE shocktrade;

-- ------------------------------------------------------------
-- Stocks
-- ------------------------------------------------------------

DROP TABLE IF EXISTS mock_stocks;
CREATE TABLE mock_stocks (
    symbol VARCHAR(12) NOT NULL,
    `exchange` VARCHAR(12) NOT NULL,
    `name` VARCHAR(255),
    sector VARCHAR(80),
    industry VARCHAR(80),
    cikNumber VARCHAR(11),
    prevClose DOUBLE,
    `open` DOUBLE,
    `close` DOUBLE,
    spread DOUBLE,
    high DOUBLE,
    low DOUBLE,
    `change` DOUBLE,
    changePct DOUBLE,
    lastTrade DOUBLE,
    tradeDateTime DATETIME,
    beta DOUBLE,
    volume BIGINT,
    avgVolume10Day BIGINT,
    PRIMARY KEY(symbol, exchange)
);


DROP TABLE IF EXISTS stocks_eoddata;
CREATE TABLE stocks_eoddata (
    symbol VARCHAR(12) NOT NULL,
    exchange VARCHAR(12) NOT NULL,
    name VARCHAR(255),
    prevClose DOUBLE,
    open DOUBLE,
    close DOUBLE,
    high DOUBLE,
    low DOUBLE,
    `change` DOUBLE,
    changePct DOUBLE,
    lastTrade DOUBLE,
    tradeDateTime DATETIME,
    volume BIGINT,
    PRIMARY KEY(symbol, exchange)
);

DROP TABLE IF EXISTS stocks_cik;
CREATE TABLE stocks_cik (
    symbol VARCHAR(12) NOT NULL,
    exchange VARCHAR(12) NOT NULL,
    name VARCHAR(128),
    cikNumber VARCHAR(20) NOT NULL,
    mailingAddress TEXT,
    PRIMARY KEY(symbol, exchange)
);

DROP TABLE IF EXISTS stocks_nasdaq;
CREATE TABLE stocks_nasdaq (
    symbol VARCHAR(12) NOT NULL,
    exchange VARCHAR(12) NOT NULL,
    name VARCHAR(128),
    lastTrade DOUBLE,
    marketCap DECIMAL(17,2),
    ADRTSO VARCHAR(80),
    IPOyear INTEGER,
    sector VARCHAR(80),
    industry VARCHAR(80),
    summary VARCHAR(80),
    quote VARCHAR(80),
    PRIMARY KEY(symbol, exchange)
);

DROP TABLE IF EXISTS stocks_wikipedia;
CREATE TABLE stocks_wikipedia (
    symbol VARCHAR(12) NOT NULL,
    name VARCHAR(128),
    sector VARCHAR(128),
    industry VARCHAR(128),
    cityState VARCHAR(128),
    initialReportingDate VARCHAR(12),
    cikNumber VARCHAR(11),
    yearFounded VARCHAR(4)
);

-- ------------------------------------------------------------
-- Stocks views
-- ------------------------------------------------------------

DROP VIEW IF EXISTS stocks;
CREATE VIEW stocks AS
SELECT
    EOD.symbol,
    EOD.exchange,
    COALESCE(EOD.name, CIK.name, WIK.name) AS name,
    EOD.lastTrade,
    EOD.tradeDateTime,
    EOD.prevClose,
    EOD.`open`,
    EOD.`close`,
    EOD.high,
    EOD.low,
    (100.0 * (EOD.high - EOD.low) / EOD.high) AS spread,
    EOD.`change`,
    COALESCE(EOD.changePct, EOD.`change` / EOD.prevClose) AS changePct,
    EOD.volume,
    NULL AS avgVolume10Day,
    NULL AS beta,
    COALESCE(CIK.cikNumber, WIK.cikNumber) AS cikNumber,
    WIK.sector,
    WIK.industry
FROM stocks_eoddata EOD
LEFT JOIN stocks_cik CIK ON CIK.symbol = EOD.symbol AND CIK.exchange = EOD.exchange
LEFT JOIN stocks_wikipedia WIK ON WIK.symbol = EOD.symbol
-- LEFT JOIN stocks_nasdaq NAS ON NAS.symbol = EOD.symbol AND NAS.exchange = EOD.exchange
;