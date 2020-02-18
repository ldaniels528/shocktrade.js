-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------
INSERT INTO users (userID, name, wallet) VALUES (uuid(), 'ldaniels', 125000);
INSERT INTO users (userID, name, wallet) VALUES (uuid(), 'gunst4rhero', 125000);
INSERT INTO users (userID, name, wallet) VALUES (uuid(), 'gadget', 125000);
INSERT INTO users (userID, name, wallet) VALUES (uuid(), 'daisy', 125000);

-- ------------------------------------------------------------
-- Contests
-- ------------------------------------------------------------
INSERT INTO contests (contestID, name, startingBalance) VALUES (uuid(), 'Winter is here', 25000);

-- ------------------------------------------------------------
-- Players
-- ------------------------------------------------------------
INSERT INTO contest_players (userID, contestID, wallet)
SELECT U.userID, C.contestID,  C.initialFunds
FROM contests C, users U;

-- ------------------------------------------------------------
-- Stocks
-- ------------------------------------------------------------
INSERT INTO stocks (stockID, symbol, exchange, lastSale, tradeDateTime) VALUES (uuid(), 'AAPL', 'NASDAQ', 167.88, now());
INSERT INTO stocks (stockID, symbol, exchange, lastSale, tradeDateTime) VALUES (uuid(), 'AMD', 'NYSE', 2.34, now());
INSERT INTO stocks (stockID, symbol, exchange, lastSale, tradeDateTime) VALUES (uuid(), 'GE', 'NYSE', 32.11, now());
