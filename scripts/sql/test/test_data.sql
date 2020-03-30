USE shocktrade;

TRUNCATE contests;
TRUNCATE orders;
TRUNCATE portfolios;
TRUNCATE positions;
-- TRUNCATE posts;
-- TRUNCATE post_tags;
TRUNCATE robots;
TRUNCATE users;

-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'ldaniels', 1000000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'fugitive528', 1000000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'gunst4rhero', 1000000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'gadget', 1000000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'daisy', 1000000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'natech', 1000000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'seralovett', 1000000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'dizorganizer', 1000000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'naughtymonkey', 1000000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'joey', 1000000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'teddy', 1000000);

-- ------------------------------------------------------------
-- Robots
-- ------------------------------------------------------------
INSERT INTO robots (username, strategy) VALUES ('daisy', 'penny-stock');
INSERT INTO robots (username, strategy) VALUES ('gadget', 'penny-stock');
INSERT INTO robots (username, strategy) VALUES ('ldaniels', 'penny-stock');
INSERT INTO robots (username, strategy) VALUES ('natech', 'penny-stock');
INSERT INTO robots (username, strategy) VALUES ('gunst4rhero', 'penny-stock');
INSERT INTO robots (username, strategy) VALUES ('seralovett', 'penny-stock');
INSERT INTO robots (username, strategy) VALUES ('dizorganizer', 'penny-stock');
INSERT INTO robots (username, strategy) VALUES ('naughtymonkey', 'penny-stock');
INSERT INTO robots (username, strategy) VALUES ('joey', 'penny-stock');
INSERT INTO robots (username, strategy) VALUES ('teddy', 'penny-stock');

-- ------------------------------------------------------------
-- User Awards
-- ------------------------------------------------------------
INSERT INTO user_awards (userAwardID, userID, awardCode) SELECT uuid(), U.userID, 'MADMONEY' FROM users U WHERE username = 'fugitive528';
INSERT INTO user_awards (userAwardID, userID, awardCode) SELECT uuid(), U.userID, 'CHKDFLAG' FROM users U WHERE username = 'fugitive528';
INSERT INTO user_awards (userAwardID, userID, awardCode) SELECT uuid(), U.userID, 'CRYSTBAL' FROM users U WHERE username = 'fugitive528';
INSERT INTO user_awards (userAwardID, userID, awardCode) SELECT uuid(), U.userID, 'GLDTRPHY' FROM users U WHERE username = 'fugitive528';
INSERT INTO user_awards (userAwardID, userID, awardCode) SELECT uuid(), U.userID, 'PAYDIRT' FROM users U WHERE username = 'fugitive528';

-- ------------------------------------------------------------
-- Posts
-- ------------------------------------------------------------
-- INSERT INTO posts (postID, userID, text, likes)
-- SELECT uuid(), userID, 'Winter is coming... Soon....', 0
-- FROM users
-- WHERE username = 'ldaniels';

-- INSERT INTO post_tags (postID, userID, hashTag)
-- SELECT P.postID, U.userID, '#gameOfThrones'
-- FROM posts P
-- INNER JOIN users U ON U.userID = P.userID
-- WHERE U.username = 'ldaniels';

-- ------------------------------------------------------------
-- Contests
-- ------------------------------------------------------------
INSERT INTO contests (contestID, hostUserID, name, statusID, startingBalance, expirationTime)
SELECT uuid(), userID, 'Winter is here', CS.statusID, 25000, DATE_ADD(now(), INTERVAL 30 DAY)
FROM users
LEFT JOIN contest_statuses CS ON CS.status = 'ACTIVE'
WHERE username = 'fugitive528';

INSERT INTO contests (contestID, hostUserID, name, statusID, startingBalance, expirationTime)
SELECT uuid(), userID, 'Winter is coming', CS.statusID, 25000, DATE_ADD(now(), INTERVAL 30 DAY)
FROM users
LEFT JOIN contest_statuses CS ON CS.status = 'ACTIVE'
WHERE username = 'ldaniels';

INSERT INTO contests (contestID, hostUserID, name, statusID, startingBalance, expirationTime)
SELECT uuid(), userID, 'Girl Power', CS.statusID, 25000, DATE_ADD(now(), INTERVAL 30 DAY)
FROM users
LEFT JOIN contest_statuses CS ON CS.status = 'ACTIVE'
WHERE username = 'daisy';

-- ------------------------------------------------------------
-- Portfolios
-- ------------------------------------------------------------
INSERT INTO portfolios (portfolioID, userID, contestID, funds)
SELECT uuid(), U.userID, C.contestID, C.startingBalance
FROM users U
INNER JOIN contests C ON C.name = 'Winter is here';

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
WHERE U.username = 'fugitive528';

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
