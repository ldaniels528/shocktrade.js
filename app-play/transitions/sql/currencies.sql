IF OBJECT_ID('dbo.currencies', 'U') IS NOT NULL
  DROP TABLE dbo.currencies;

CREATE TABLE dbo.currencies (
  currency_id     INTEGER PRIMARY KEY             IDENTITY (1, 1),
  currency_code   VARCHAR(12) NOT NULL,
  creationTime    DATETIME    NOT NULL            DEFAULT getdate(),
  lastUpdatedTime DATETIME
)