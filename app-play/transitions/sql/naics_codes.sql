IF OBJECT_ID('dbo.naics_codes', 'U') IS NOT NULL
  DROP TABLE dbo.naics_codes;

CREATE TABLE dbo.naics_codes (
  uid           INT PRIMARY KEY IDENTITY (1, 1),
  naicsNumber   INT  NOT NULL,
  description   TEXT NOT NULL,
  creation_time DATE NOT NULL   DEFAULT getdate()
);

CREATE UNIQUE INDEX naics_codes_xpk ON dbo.naics_codes (naicsNumber);