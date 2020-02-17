
INSERT INTO users (userID, name, funds) VALUES (uuid(), 'ldaniels', 125000);
INSERT INTO users (userID, name, funds) VALUES (uuid(), 'gunst4rhero', 125000);
INSERT INTO users (userID, name, funds) VALUES (uuid(), 'gadget', 125000);
INSERT INTO users (userID, name, funds) VALUES (uuid(), 'daisy', 125000);

INSERT INTO contests (contestID, name, initialFunds) VALUES (uuid(), 'Winter is here', 25000);

INSERT INTO players (userID, contestID, funds)
SELECT U.userID, C.contestID,  C.initialFunds
FROM contests C, users U;

INSERT INTO stocks (stockID, symbol, exchange, lastSale, tradeDateTime) VALUES (uuid(), 'AAPL', 'NASDAQ', 167.88, now());
INSERT INTO stocks (stockID, symbol, exchange, lastSale, tradeDateTime) VALUES (uuid(), 'AMD', 'NYSE', 2.34, now());
INSERT INTO stocks (stockID, symbol, exchange, lastSale, tradeDateTime) VALUES (uuid(), 'GE', 'NYSE', 32.11, now());
