-----------------------------------------------------
--	These indices will speed up searches and updates
-----------------------------------------------------
CREATE INDEX TradingHistory_IDX1 ON TradingHistory ( symbol, tradeDate );
CREATE INDEX TradingHistory_IDX2 ON TradingHistory ( exported, tradeDate );

-----------------------------------------------------
--	These queries support exported data to Cassandra
-----------------------------------------------------

SELECT MAX(id) FROM TradingHistory WHERE exported = 0;

-----------------------------------------------------
--	This query will update the spread
-----------------------------------------------------
UPDATE TradingHistory 
SET spread = 100.0 * ((high - low)/low) 
WHERE spread IS NULL;


-----------------------------------------------------
--	This materialized view makes it possible to easily
--	retrieve the previous trading date for any given 
--	trading date.
-----------------------------------------------------

/*
+--------+------------+---------------+
| symbol | tradeDate  | prevTradeDate |
+--------+------------+---------------+
| AAPL   | 2012-10-08 | 2012-10-05    |
| AAPL   | 2012-10-05 | 2012-10-04    |
| AAPL   | 2012-10-04 | 2012-10-03    |
| AAPL   | 2012-10-03 | 2012-10-02    |
| AAPL   | 2012-10-02 | 2012-10-01    |
+--------+------------+---------------+
 */
CREATE TABLE TradingDates
AS
SELECT B.symbol, B.tradeDate, MAX(A.tradeDate) AS prevTradeDate
FROM TradingHistory B
INNER JOIN TradingHistory A ON A.symbol = B.symbol
WHERE A.tradeDate < B.tradeDate
GROUP BY B.symbol, B.tradeDate;

--------------------------------------------------------------
-- Updates the prevTradeDate in TradingHistory from TradingDates
--------------------------------------------------------------
UPDATE TradingHistory H 
INNER JOIN TradingDates D ON ( D.symbol = H.symbol AND D.tradeDate = H.tradeDate )
SET H.prevTradeDate = D.prevTradeDate
WHERE H.prevTradeDate IS NULL;

--------------------------------------------------------------
--	Adds new records into TradingDates from TradingHistory
--------------------------------------------------------------
INSERT INTO TradingDates ( symbol, tradeDate, prevTradeDate )
SELECT B.symbol, B.tradeDate, MAX(A.tradeDate) AS prevTradeDate FROM TradingHistory B
INNER JOIN TradingHistory A ON A.symbol = B.symbol
WHERE A.tradeDate < B.tradeDate
AND B.tradeDate > ( SELECT MAX(tradeDate) FROM TradingDates )
GROUP BY B.symbol, B.tradeDate;

-----------------------------------------------------
--	This query will update the chg and changePct fields
-----------------------------------------------------
UPDATE TradingHistory B
INNER JOIN TradingDates C ON ( C.symbol = B.symbol AND C.tradeDate = B.tradeDate )
INNER JOIN TradingHistory A ON ( A.symbol = C.symbol AND A.tradeDate = C.prevTradeDate ) 
SET B.chg = ( B.close - A.close ), 
	B.changePct = 100.0 * ( B.close - A.close )/A.close,
	B.spread = 100.0 * ( B.high - B.low )/B.low,
	B.prevClose = A.close
WHERE B.chg IS NULL; 


-----------------------------------------------------
--	This transformation query copies data from the old
--	layout to the new layout.
--	@Depcreated
-----------------------------------------------------
INSERT INTO TradingHistory ( symbol, exchange, tradeDate, open, close, high, low, volume )
SELECT symbol, exchange, tradeDate, open, close, high, low, volume 
FROM stocks.HistoricalQuotes
ORDER BY tradeDate ASC;


-----------------------------------------------------
--	Miscellaneous Queries
-----------------------------------------------------
SELECT h.symbol, h.prevClose, h.open, h.close, h.chg, h.changePct, h.tradeDate
FROM TradingHistory h,
( SELECT tradeDate, MAX(changePct) AS changePct FROM TradingHistory 
WHERE tradeDate BETWEEN '2012-08-01' AND '2012-08-09' AND changePct > 0 GROUP BY tradeDate ) AS t 
WHERE h.tradeDate = t.tradeDate
AND h.changePct = t.changePct
ORDER BY h.tradeDate ASC
LIMIT 30;


SELECT symbol, changePct, tradeDate
FROM TradingHistory
GROUP BY symbol
ORDER BY changePct DESC


SELECT B.symbol, B.changePct, MAX(A.changePct) AS maxChangePct
FROM TradingHistory B
INNER JOIN TradingHistory A ON A.symbol = B.symbol
WHERE A.changePct < B.changePct
GROUP BY B.symbol, B.tradeDate
ORDER BY changePct DESC
LIMIT 5;


SELECT symbol, changePct, tradeDate
FROM TradingHistory 
WHERE changePct = (
	SELECT MAX(changePct) FROM TradingHistory
	WHERE tradeDate = '2012-07-11'
);


