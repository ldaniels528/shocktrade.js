GRANT FILE ON shocktrade.* to webapp;

LOAD DATA INFILE './files/NYSE_20200221.txt'
INTO TABLE stocks_nasdaq
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(symbol, tradeDateTime, open, high, low, close, volume)
;
