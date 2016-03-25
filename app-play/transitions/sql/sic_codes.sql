IF OBJECT_ID('dbo.sic_codes', 'U') IS NOT NULL
  DROP TABLE dbo.sic_codes;

CREATE TABLE dbo.sic_codes (
  uid   INT PRIMARY KEY IDENTITY (1, 1),
  sicNumber     INT  NOT NULL,
  description   TEXT NOT NULL,
  creation_time DATE NOT NULL   DEFAULT getdate()
);

CREATE UNIQUE INDEX sic_codes_xpk ON dbo.sic_codes (sicNumber);