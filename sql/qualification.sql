USE shocktrade;

DROP VIEW IF EXISTS qualifications;
CREATE VIEW qualifications AS
SELECT 
	P.userID, O.*, 
    CASE WHEN priceType IN ('MARKET', 'MARKET_AT_CLOSE') THEN S.close ELSE S.lastTrade END AS lastTrade, 
    S.tradeDateTime, (S.lastTrade * O.quantity + 9.99) AS cost, P.funds, P.funds - (S.lastTrade * O.quantity) as fundsLeft
FROM orders O
INNER JOIN portfolios P ON P.portfolioID = O.portfolioID
INNER JOIN stocks S ON S.symbol = O.symbol AND S.exchange = O.exchange
WHERE orderType = 'BUY' 
AND O.closed = 0
AND (S.tradeDateTime >= O.creationTime AND (O.expirationTime IS NULL OR S.tradeDateTime <= O.expirationTime))
AND (priceType IN ('MARKET', 'MARKET_AT_CLOSE') OR (priceType = 'LIMIT' AND O.price <= S.lastTrade))
AND P.funds - (S.lastTrade * O.quantity) >= 0
;

SELECT * FROM qualifications;
 