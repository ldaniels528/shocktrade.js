INSERT INTO KeyStatistics (
symbol, EBITDA, avgVolume10Day, avgVolume3Month,
beta, bookValuePerShare, change52Week, change52WeekSNP500,
currentRatio,dilutedEPS,dividendDate,dividendYield5YearAvg,
earningsGrowthQuarterly,enterpriseValue,enterpriseValueOverEBITDA,
enterpriseValueOverRevenue,errorMessage,exDividendDate,expirationTime,
failures,fiscalYearEndDate,forwardAnnualDividendRate,forwardAnnualDividendYield ,
forwardPE,grossProfit,high52Week ,inactive,lastSplitDate ,lastSplitFactor,
lastUpdatedTime,leveredFreeCashFlow,low52Week,marketCapIntraday,mostRecentQuarterDate ,
movingAverage200Day,movingAverage50Day ,netIncomeAvailToCommon,operatingCashFlow,
operatingMargin,payoutRatio,pctHeldByInsiders,pctHeldByInstitutions ,pegRatio5YearExp,
priceOverBookValue ,priceOverSales,profitMargin,returnOnAssets,returnOnEquity,
revenue,revenueGrowthQuarterly,revenuePerShare,sharesFloat,sharesOutstanding ,sharesShort,sharesShortPriorMonth ,
shortPctOfFloat,shortRatio ,totalCash,totalCashPerShare,totalDebt,totalDebtOverEquity,trailingAnnualDividendYield,trailingPE 
)
SELECT
symbol, EBITDA, avgVolume10Day, avgVolume3Month,
beta, bookValuePerShare, change52Week, change52WeekSNP500,
currentRatio,dilutedEPS,dividendDate,dividendYield5YearAvg,
earningsGrowthQuarterly,enterpriseValue,enterpriseValueOverEBITDA,
enterpriseValueOverRevenue,errorMessage,exDividendDate,expirationTime,
failures,fiscalYearEndDate,forwardAnnualDividendRate,forwardAnnualDividendYield ,
forwardPE,grossProfit,high52Week ,inactive,lastSplitDate ,lastSplitFactor,
lastUpdatedTime,leveredFreeCashFlow,low52Week,marketCapIntraday,mostRecentQuarterDate ,
movingAverage200Day,movingAverage50Day ,netIncomeAvailToCommon,operatingCashFlow,
operatingMargin,payoutRatio,pctHeldByInsiders,pctHeldByInstitutions ,pegRatio5YearExp,
priceOverBookValue ,priceOverSales,profitMargin,returnOnAssets,returnOnEquity,
revenue,revenueGrowthQuarterly,revenuePerShare,sharesFloat,sharesOutstanding ,sharesShort,sharesShortPriorMonth ,
shortPctOfFloat,shortRatio ,totalCash,totalCashPerShare,totalDebt,totalDebtOverEquity,trailingAnnualDividendYield,trailingPE                 
FROM ComprehensiveQuotes
WHERE symbol NOT IN (
	SELECT symbol FROM KeyStatistics
)
AND inactive = 0;


INSERT INTO Users ( name, password ) SELECT name, password FROM stocks.Users;

INSERT INTO Simulations ( name, funds, originalFunds ) 
SELECT name, funds, originalFunds FROM stocks.Simulations;

INSERT INTO Positions ( simulationId, symbol, shares, purchasedPrice, purchasedDate )
SELECT  simulationId, symbol, shares, purchasedPrice, purchasedDate FROM stocks.Positions;

INSERT INTO UserSimulations ( simulationId, userId )
SELECT simulationId, userId FROM stocks.UserSimulations;

INSERT INTO Competitions ( name ) SELECT name FROM stocks.Competitions;
INSERT INTO CompetitionSimulations ( simulation_id ) SELECT simulation_id FROM stocks.CompetitionSimulations;

SELECT * FROM OnlineQuotes LIMIT 20
SELECT * FROM HistoricalQuotes LIMIT 20
UPDATE OnlineQuotes SET tradeDate = date
UPDATE HistoricalQuotes SET tradeDate = date
ALTER TABLE HistoricalQuotes DROP COLUMN date
ALTER TABLE OnlineQuotes DROP COLUMN date


INSERT INTO OnlineQuotes ( symbol  ) 
SELECT symbol FROM HistoricalQuotes GROUP BY symbol;

UPDATE OnlineQuotes O
INNER JOIN HistoricalQuotes H ON H.symbol = O.symbol
SET O.exchange = H.exchange;

SELECT * FROM OnlineQuotes;


SELECT H.*, O.* FROM HistoricalQuotes H 
INNER JOIN OnlineQuotes O ON O.symbol = H.symbol
WHERE H.symbol = 'AAPL';

SELECT * FROM OnlineQuotes WHERE close IS NOT NULL LIMIT 10;	

SELECT *, 0 as id
FROM OnlineQuotes
WHERE volume >= 500000 AND chg < 0.0000 AND close <= 1.0000 AND exchange IN ( 'AMEX','NASDAQ','NYSE' ) 
ORDER BY date ASC
LIMIT 10;

SELECT c.* FROM Competitions AS c 
INNER JOIN CompetitionSimulations AS cs ON cs.competition_id = c.id 
INNER JOIN Simulations AS s ON s.id = cs.simulation_id 
INNER JOIN UserSimulations AS us ON us.simulationId = cs.simulation_id 
INNER JOIN Users AS u ON u.id = us.userId 
WHERE u.id = 1;


SELECT u.name, s.* FROM CompetitionSimulations AS cs
INNER JOIN Simulations AS s ON s.id = cs.simulation_id
INNER JOIN UserSimulations AS us ON us.simulationId = cs.simulation_id 
INNER JOIN Users AS u ON u.id = us.userId;

SELECT 
	u.id as userId, 
	u.name as userName, 
	s.id as simulationId, 
	s.name as simulationName,
	s.originalFunds,
	s.funds
FROM CompetitionSimulations AS cs
INNER JOIN Simulations AS s ON s.id = cs.simulation_id
INNER JOIN UserSimulations AS us ON us.simulationId = cs.simulation_id 
INNER JOIN Users AS u ON u.id = us.userId ;