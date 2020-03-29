USE shocktrade;

update stocks_eoddata set prevClose = `close` - `change`;
update stocks_eoddata set prevClose = `close` - `change`;
update stocks_eoddata set changePct = `change` / prevClose WHERE prevClose > 0;
update stocks_eoddata set spread = 100.0 * (high - low)/high;
update stocks_eoddata set lastTrade = (high + low) / 2, tradeDateTime = '2020-02-21 16:59:59' where lastTrade is null;
update stocks_eoddata set `open` = prevClose - `change` where open is null;

update mock_stocks MS
inner join stocks_eoddata S ON S.symbol = MS.symbol
set MS.lastTrade = S.lastTrade;

SELECT * FROM stocks
WHERE volume >= 1000 AND lastTrade <= 30
ORDER BY volume DESC
LIMIT 250;


SELECT
    C.contestID,
    C.name,
    R.totalEquity,
    R.gainLoss,
    C.friendsOnly,
    C.invitationOnly,
    C.levelCap,
    C.perksAllowed,
    C.robotsAllowed
FROM contests C
LEFT JOIN portfolios CP ON CP.contestID = C.contestID AND CP.userID = '47d09c0a-55d2-11ea-a02d-0800273905de'
LEFT JOIN (
    SELECT
        P.portfolioID,
        P.userID,
        P.funds + SUM(S.lastTrade) AS totalEquity,
        (P.funds + SUM(S.lastTrade)) / C.startingBalance AS gainLoss
    FROM portfolios P
    INNER JOIN contests C ON C.contestID = P.contestID
    INNER JOIN positions PS ON PS.portfolioID = P.portfolioID
    INNER JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
    WHERE P.contestID = C.contestID
    ORDER BY totalEquity DESC
) AS R ON R.portfolioID = CP.portfolioID AND R.userID = CP.userID
WHERE C.userID = '47d09c0a-55d2-11ea-a02d-0800273905de';

