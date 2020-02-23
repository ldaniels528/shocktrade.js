grant file on *.* to webapp identified by 'kentest1';

LOAD DATA INFILE './files/NYSE_20200221.txt'
INTO TABLE stocks
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(symbol, tradeDateTime, open, high, low, close, volume)
;
