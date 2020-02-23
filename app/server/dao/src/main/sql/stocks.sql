-- ------------------------------------------------------------
-- Stocks
-- ------------------------------------------------------------

DROP TABLE IF EXISTS stocks_eoddata;
CREATE TABLE stocks_eoddata (
    symbol VARCHAR(12) NOT NULL,
    exchange VARCHAR(12) NOT NULL,
    companyName VARCHAR(255),
    prevClose DECIMAL(12,5),
    open DECIMAL(12,5),
    close DECIMAL(12,5),
    high DECIMAL(12,5),
    low DECIMAL(12,5),
    spread DECIMAL(12,5),
    `change` DECIMAL(12,5),
    changePct DECIMAL(12,5),
    lastTrade DECIMAL(12,5),
    tradeDateTime DATETIME,
    volume BIGINT,
    avgVolume10Day BIGINT,
    beta DECIMAL(7,4),
    active BIT NOT NULL DEFAULT 0,
    PRIMARY KEY(symbol, exchange)
);

DROP TABLE IF EXISTS stocks_cik;
CREATE TABLE stocks_cik (
    symbol VARCHAR(12) NOT NULL,
    exchange VARCHAR(12) NOT NULL,
    cikNumber VARCHAR(20) NOT NULL,
    companyName VARCHAR(128),
    mailingAddress TEXT,
    PRIMARY KEY(symbol, exchange)
);

DROP TABLE IF EXISTS stocks_nasdaq;
CREATE TABLE stocks_nasdaq (
    symbol VARCHAR(12) NOT NULL,
    exchange VARCHAR(12) NOT NULL,
    cikNumber VARCHAR(20) NOT NULL,
    companyName VARCHAR(128),
    lastTrade DECIMAL(12,5),
    marketCap DECIMAL(17,2),
    ADRTSO VARCHAR(80),
    IPOyear INTEGER,
    sector VARCHAR(80),
    industry VARCHAR(80),
    summary VARCHAR(80),
    quote VARCHAR(80),
    PRIMARY KEY(symbol, exchange)
);

DROP VIEW IF EXISTS stocks;
CREATE VIEW stocks AS
SELECT
    EOD.symbol,
    EOD.exchange,
    COALESCE(EOD.companyName, CIK.companyName) AS companyName,
    EOD.lastTrade,
    EOD.tradeDateTime,
    EOD.prevClose,
    EOD.`open`,
    EOD.`close`,
    EOD.high,
    EOD.low,
    (100.0 * (EOD.high - EOD.low) / EOD.high) AS spread,
    EOD.`change`,
    (EOD.`change` / EOD.prevClose) AS changePct,
    EOD.volume,
    EOD.avgVolume10Day,
    EOD.beta,
    CIK.cikNumber,
    EOD.active
FROM stocks_eoddata EOD
LEFT JOIN stocks_cik CIK ON CIK.symbol = EOD.symbol AND CIK.exchange = EOD.exchange
-- LEFT JOIN stocks_nasdaq NAS ON NAS.symbol = EOD.symbol AND NAS.exchange = EOD.exchange
;