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
INSERT INTO contests (contestID, name, startingBalance, expirationTime) VALUES (uuid(), 'Winter is here', 25000, DATE_ADD(now(), INTERVAL 30 DAY));

-- ------------------------------------------------------------
-- Players
-- ------------------------------------------------------------
INSERT INTO contest_players (playerID, userID, contestID, funds)
SELECT uuid(), U.userID, C.contestID, C.startingBalance
FROM contests C, users U;

-- ------------------------------------------------------------
-- Stocks
-- ------------------------------------------------------------
--INSERT INTO stocks (_id, symbol, exchange, lastTrade, tradeDateTime) VALUES (uuid(), 'AAPL', 'NASDAQ', 167.88, now());
--INSERT INTO stocks (_id, symbol, exchange, lastTrade, tradeDateTime) VALUES (uuid(), 'AMD', 'NYSE', 2.34, now());
--INSERT INTO stocks (_id, symbol, exchange, lastTrade, tradeDateTime) VALUES (uuid(), 'GE', 'NYSE', 32.11, now());
