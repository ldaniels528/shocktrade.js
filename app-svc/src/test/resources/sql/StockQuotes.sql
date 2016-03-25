-- upcoming dividends
SELECT Q.symbol, S.exchange, Q.dividendPayDate, DATE_ADD(dividendPayDate, INTERVAL 1 YEAR) nextDividendPayDate
FROM StockQuotes Q 
INNER JOIN Securities S ON S.symbol = Q.symbol 
WHERE S.valid = 1 
AND divYield IS NOT NULL 
AND DATE_ADD(dividendPayDate, INTERVAL 1 YEAR) < DATE_ADD(now(), INTERVAL 30 DAY) 
LIMIT 25;

-- Composite quotes view
CREATE VIEW CompositeQuotes
AS
SELECT S.*, K.* FROM StockQuotes S
INNER JOIN KeyStatistics K ON K.symbol = S.symbol;

-- OTC base symbols
SELECT A.symbol, A.name, A.creationTime, A.valid, B.symbol, B.name, B.creationTime, B.valid
FROM Securities A, Securities B
WHERE LENGTH(A.symbol) = 4
AND LENGTH(B.symbol) = 5
AND A.symbol = LEFT(B.symbol,LENGTH(A.symbol))
AND A.exchange = B.exchange
AND A.exchange = 'OTCBB'
LIMIT 10;


SELECT S.sector, S.industry, S.symbol, S.name, Q.chg, Q.changePct  
FROM Securities S INNER JOIN StockQuotes Q ON Q.symbol = S.symbol  
WHERE active = 1;

SELECT COUNT(*)
FROM Securities A, Securities B
WHERE LENGTH(A.symbol) = 4
AND LENGTH(B.symbol) = 5
AND A.symbol = LEFT(B.symbol,LENGTH(A.symbol))
AND A.exchange = B.exchange
AND A.exchange = 'OTCBB';


-- Securities: differences in exchange
SELECT S.symbol, S.exchange, Q.exchange 
FROM Securities S 
INNER JOIN StockQuotes Q ON Q.symbol = S.symbol 
WHERE S.exchange <> Q.exchange LIMIT 10;
 

-- update change & spread
UPDATE StockQuotes 
SET chg = (close - prevClose)/close, changePct = ((close - prevClose)/close),
spread = (high - low)/low
WHERE chg IS NULL
AND close IS NOT NULL
AND prevClose IS NOT NULL;


SELECT symbol, exchange, chg, changePct, S.tradeDate 
FROM StockQuotes S 
INNER JOIN ( SELECT MAX(tradeDate) tradeDate FROM StockQuotes ) AS T ON S.tradeDate = T.tradeDate
WHERE chg > 0
AND exchange NOT IN ( 'CCY', 'OTCBB' )  
ORDER BY changePct DESC
LIMIT 5;


UPDATE StockQuotes 
SET chg = CAST( close - prevClose AS DECIMAL(11,4)), 
	changePct = CAST( (close - prevClose)/close AS DECIMAL(4,3))
WHERE close IS NOT NULL
AND prevClose IS NOT NULL;


UPDATE StockQuotes Q INNER JOIN Securities S ON S.symbol = Q.symbol SET Q.industry = S.industry, Q.sector = S.sector;


UPDATE StockQuotes SET symbol = SUBSTRING(symbol,1,LENGTH(symbol)-3) WHERE symbol LIKE '%.OB';

UPDATE TradingHistory SET symbol = SUBSTRING(symbol,1,LENGTH(symbol)-3) WHERE symbol LIKE '%.OB';

SELECT B.symbol FROM StockQuotes A, StockQuotes B WHERE concat( A.symbol, '.OB' ) = B.symbol;


INSERT INTO DeleteSymbols ( symbol ) 
SELECT B.symbol FROM StockQuotes A, StockQuotes B WHERE concat( A.symbol, '.OB' ) = B.symbol;