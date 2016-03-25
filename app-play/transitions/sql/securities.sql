IF OBJECT_ID('dbo.securities', 'U') IS NOT NULL
  DROP TABLE dbo.securities;

CREATE TABLE dbo.securities (
  securities_id     INTEGER PRIMARY KEY           IDENTITY (1, 1),
  symbol            VARCHAR(12) NOT NULL,
  exchange          VARCHAR(8)  NOT NULL,
  name              VARCHAR(64),
  sector            VARCHAR(64),
  industry          VARCHAR(64),
  subIndustry       VARCHAR(64),
  sicNumber         VARCHAR(32),
  naicsNumber       VARCHAR(32),
  cikNumber         VARCHAR(32),
  prevClose         DECIMAL(12, 5),
  [open]            DECIMAL(12, 5),
  lastSale          DECIMAL(12, 5),
  [close]           DECIMAL(12, 5),
  low               DECIMAL(12, 5),
  low52Week         DECIMAL(12, 5),
  high              DECIMAL(12, 5),
  high52Week        DECIMAL(12, 5),
  spread            DECIMAL(12, 5),
  tradeDate         DATETIME,
  ask               DECIMAL(12, 5),
  askSize           INTEGER,
  bid               DECIMAL(12, 5),
  bidSize           INTEGER,
  volume            BIGINT,
  avgVolume         BIGINT,
  bookValuePerShare DECIMAL(12, 5),
  change            DECIMAL(12, 5),
  changePct         DECIMAL(8, 4),
  marketCap         DECIMAL(12, 5),
  target1Y          DECIMAL(12, 5),
  creationTime      DATETIME    NOT NULL          DEFAULT getdate(),
  lastUpdatedTime   DATETIME,
  active            BIT
);

CREATE UNIQUE INDEX securities_xpk ON dbo.securities (symbol, exchange);