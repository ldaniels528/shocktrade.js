

update stocks_eoddata set prevClose = `close` - `change`;
update stocks_eoddata set prevClose = `close` - `change`;
update stocks_eoddata set changePct = `change` / prevClose WHERE prevClose > 0;
update stocks_eoddata set spread = 100.0 * (high - low)/high;
update stocks_eoddata set lastTrade = (high + low) / 2, tradeDateTime = '2020-02-21 16:59:59' where lastTrade is null;
update stocks_eoddata set `open` = prevClose - `change` where open is null;


INSERT INTO stocks_eoddata (symbol, exchange, companyName, high, low, `close`, volume, `change`, changePct)
SELECT symbol, exchange, name, high, low, `close`, volume, `change`, changePct
FROM stocks;


SELECT * FROM stocks
WHERE volume >= 1000 AND lastTrade <= 30
ORDER BY volume DESC
LIMIT 250;