TRUNCATE TABLE StockExchanges;


UPDATE StockExchanges AS S
INNER JOIN (
	SELECT exchange, MIN(tradeDate) earliestDate, MAX(tradeDate) lastestDate, COUNT(*) total 
	FROM TradingHistory 
	GROUP BY exchange
) AS T ON T.exchange = S.exchange
SET S.earliestDate = T.earliestDate,
	S.lastestDate = T.lastestDate,
	S.symbols = T.symbols,
	S.total = T.total;




INSERT INTO StockExchanges ( exchange, latestDate, total ) 
SELECT exchange, MAX(tradeDate) lastestDate, COUNT(*) total FROM TradingHistory 
GROUP BY exchange 
ORDER BY exchange ASC;







UPDATE StockQuotes SET chg = CAST(chg AS DECIMAL(11,4)), spread = CAST(spread AS DECIMAL(11,4));

INSERT INTO StockExchanges ( exchange, latestDate, total ) 
SELECT exchange, MAX(tradeDate) lastestDate, COUNT(*) total FROM TradingHistory 
GROUP BY exchange 
ORDER BY exchange ASC;

DROP PROCEDURE updateStockExchangeStatistics;

DELIMITER //
CREATE PROCEDURE updateStockExchangeStatistics()
BEGIN
	TRUNCATE TABLE StockExchanges;
	
	INSERT INTO StockExchanges ( exchange, latestDate, total )
	SELECT exchange, MAX(date) lastestDate, COUNT(*) total 
	FROM HistoricalQuotes 
	GROUP BY exchange
	ORDER BY exchange ASC;
END //
DELIMITER;

UPDATE OnlineQuotes SET exchange = 'NASDAQ' WHERE exchange IN ( 'NCM', 'NGM', 'NASDAQNM' )