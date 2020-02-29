USE shocktrade;

TRUNCATE contests;
TRUNCATE portfolios;
TRUNCATE orders;
TRUNCATE positions;

-- ------------------------------------------------------------
-- Contests
-- ------------------------------------------------------------
INSERT INTO contests (contestID, hostUserID, name, startingBalance, expirationTime)
SELECT uuid(), userID, 'Winter is here', 25000, DATE_ADD(now(), INTERVAL 30 DAY)
FROM users
WHERE username = 'ldaniels528';

INSERT INTO contests (contestID, hostUserID, name, startingBalance, expirationTime)
SELECT uuid(), userID, 'Winter is coming', 25000, DATE_ADD(now(), INTERVAL 30 DAY)
FROM users
WHERE username = 'ldaniels';

INSERT INTO contests (contestID, hostUserID, name, startingBalance, expirationTime)
SELECT uuid(), userID, 'Girl Power', 25000, DATE_ADD(now(), INTERVAL 30 DAY)
FROM users
WHERE username = 'daisy';

-- ------------------------------------------------------------
-- Portfolios
-- ------------------------------------------------------------
INSERT INTO portfolios (portfolioID, userID, contestID, funds)
SELECT uuid(), U.userID, C.contestID, C.startingBalance
FROM users U
INNER JOIN contests C ON C.name = 'Winter is coming';

INSERT INTO portfolios (portfolioID, userID, contestID, funds)
SELECT uuid(), U.userID, C.contestID, C.startingBalance
FROM users U
INNER JOIN contests C ON C.name = 'Girl Power'
WHERE U.username = 'daisy';

-- ------------------------------------------------------------
-- Orders
-- ------------------------------------------------------------
INSERT INTO orders (orderID, portfolioID, orderType, symbol, `exchange`, price, priceType, quantity, creationTime)
SELECT uuid(), P.portfolioID, 'BUY', S.symbol, S.`exchange`, S.lastTrade, 'MARKET', 1000, temp.maxTradeDateTime
FROM portfolios P
INNER JOIN users U ON U.userID = P.userID
INNER JOIN stocks S ON S.symbol = 'CYDY' AND S.exchange = 'OTCBB'
INNER JOIN (SELECT MAX(tradeDateTime) AS maxTradeDateTime FROM stocks) AS temp
WHERE U.username = 'ldaniels528';

INSERT INTO orders (orderID, portfolioID, orderType, symbol, `exchange`, price, priceType, quantity, creationTime)
SELECT uuid(), P.portfolioID, 'BUY', S.symbol, S.`exchange`, S.lastTrade, 'MARKET', 1000, temp.maxTradeDateTime
FROM portfolios P
INNER JOIN users U ON U.userID = P.userID
INNER JOIN stocks S ON S.symbol = 'ACTTW' AND S.exchange = 'NASDAQ'
INNER JOIN (SELECT MAX(tradeDateTime) AS maxTradeDateTime FROM stocks) AS temp
WHERE U.username = 'ldaniels';

INSERT INTO orders (orderID, portfolioID, orderType, symbol, `exchange`, price, priceType, quantity, creationTime)
SELECT uuid(), P.portfolioID, 'BUY', S.symbol, S.`exchange`, S.lastTrade, 'MARKET', 1000, temp.maxTradeDateTime
FROM portfolios P
INNER JOIN users U ON U.userID = P.userID
INNER JOIN stocks S ON S.symbol = 'VXRT' AND S.exchange = 'NASDAQ'
INNER JOIN (SELECT MAX(tradeDateTime) AS maxTradeDateTime FROM stocks) AS temp
WHERE U.username = 'daisy';

INSERT INTO orders (orderID, portfolioID, orderType, symbol, `exchange`, price, priceType, quantity, creationTime)
SELECT uuid(), P.portfolioID, 'BUY', S.symbol, S.`exchange`, S.lastTrade, 'MARKET', 1000, temp.maxTradeDateTime
FROM portfolios P
INNER JOIN users U ON U.userID = P.userID
INNER JOIN stocks S ON S.symbol = 'SEEL' AND S.exchange = 'NASDAQ'
INNER JOIN (SELECT MAX(tradeDateTime) AS maxTradeDateTime FROM stocks) AS temp
WHERE U.username = 'daisy';

INSERT INTO orders (orderID, portfolioID, orderType, symbol, `exchange`, price, priceType, quantity, creationTime)
SELECT uuid(), P.portfolioID, 'BUY', S.symbol, S.`exchange`, S.lastTrade, 'MARKET', 1000, temp.maxTradeDateTime
FROM portfolios P
INNER JOIN users U ON U.userID = P.userID
INNER JOIN stocks S ON S.symbol = 'NBRV' AND S.exchange = 'NASDAQ'
INNER JOIN (SELECT MAX(tradeDateTime) AS maxTradeDateTime FROM stocks) AS temp
WHERE U.username = 'daisy';

INSERT INTO orders (orderID, portfolioID, orderType, symbol, `exchange`, price, priceType, quantity, creationTime)
SELECT uuid(), P.portfolioID, 'BUY', S.symbol, S.`exchange`, S.lastTrade, 'MARKET', 1000, temp.maxTradeDateTime
FROM portfolios P
INNER JOIN users U ON U.userID = P.userID
INNER JOIN stocks S ON S.symbol = 'CMRX' AND S.exchange = 'NASDAQ'
INNER JOIN (SELECT MAX(tradeDateTime) AS maxTradeDateTime FROM stocks) AS temp
WHERE U.username = 'daisy';


