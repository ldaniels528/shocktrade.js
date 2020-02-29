-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'ldaniels', 125000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'gunst4rhero', 125000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'gadget', 125000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'daisy', 125000);

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


-- ------------------------------------------------------------
-- Portfolios
-- ------------------------------------------------------------
INSERT INTO portfolios (portfolioID, userID, contestID, funds)
SELECT uuid(), U.userID, C.contestID, C.startingBalance
FROM contests C, users U;

-- ------------------------------------------------------------
-- Orders
-- ------------------------------------------------------------
INSERT INTO orders (orderID, portfolioID, orderType, symbol, `exchange`, price, priceType, quantity)
SELECT uuid(), P.portfolioID, 'BUY', S.symbol, S.`exchange`, S.lastTrade, 'MARKET', 1000
FROM portfolios P
INNER JOIN users U ON U.userID = P.userID
INNER JOIN stocks S ON S.symbol = 'CYDY' AND S.exchange = 'OTCBB'
WHERE U.username = 'ldaniels528';

INSERT INTO orders (orderID, portfolioID, orderType, symbol, `exchange`, price, priceType, quantity)
SELECT uuid(), P.portfolioID, 'BUY', S.symbol, S.`exchange`, S.lastTrade, 'MARKET', 1000
FROM portfolios P
INNER JOIN users U ON U.userID = P.userID
INNER JOIN stocks S ON S.symbol = 'ACTTW' AND S.exchange = 'NASDAQ'
WHERE U.username = 'ldaniels';

-- ------------------------------------------------------------
-- Positions
-- ------------------------------------------------------------
INSERT INTO positions (positionID, portfolioID, userID, orderID, symbol, exchange, price, quantity, tradeDateTime)
SELECT uuid(), P.portfolioID, U.userID, O.orderID, O.symbol, O.exchange, O.price, O.quantity, O.creationTime
FROM users U
INNER JOIN portfolios P ON P.userID = U.userID
INNER JOIN orders O ON O.portfolioID = P.portfolioID;

-- ------------------------------------------------------------
-- Posts
-- ------------------------------------------------------------
INSERT INTO posts (postID, userID, text, likes)
SELECT uuid(), userID, 'Winter is coming... Soon....', 0
FROM users
WHERE username = 'ldaniels528';

INSERT INTO post_tags (postID, userID, hashTag)
SELECT P.postID, U.userID, '#gameOfThrones'
FROM posts P
INNER JOIN users U ON U.userID = P.userID
WHERE U.username = 'ldaniels528';