---------------------------------------------------------------------------------
-- imports securities from stock quotes
---------------------------------------------------------------------------------
INSERT INTO Securities ( creationTime, symbol, name, exchange, assetType, valid )
SELECT now(), symbol, name, exchange, 'COMMON_STOCK', 1 FROM StockQuotes 
WHERE symbol NOT IN ( SELECT symbol FROM Securities );


---------------------------------------------------------------------------------
-- imports securities from staging
---------------------------------------------------------------------------------
INSERT INTO Securities ( symbol, exchange, name, industry, sector, summaryQuote, assetType, valid, creationTime )
SELECT symbol, exchange, name, industry, sector, summaryQuote, assetType, valid, creationTime
FROM StagingSecurities 
WHERE symbol NOT IN ( SELECT symbol FROM Securities );


---------------------------------------------------------------------------------
-- de-dups the staging table
---------------------------------------------------------------------------------
DELETE FROM StagingSecurities WHERE id IN (
SELECT id FROM (
SELECT B.id FROM StagingSecurities A, StagingSecurities B 
WHERE B.symbol = A.symbol AND ( ( ( A.id > B.id ) AND ( A.industry IS NOT NULL AND B.industry IS NULL ) ) OR ( A.id < B.id ) )
) AS T
);

SELECT A.id, A.symbol, A.name, A.industry, B.id, B.symbol, B.name, B.industry FROM StagingSecurities A
INNER JOIN StagingSecurities B ON B.symbol = A.symbol
WHERE ( 	 
	( ( A.id > B.id ) AND ( A.industry IS NOT NULL AND B.industry IS NULL ) )
	OR
	( A.id < B.id )	
)
LIMIT 5;


---------------------------------------------------------------------------------
-- check to see if some securities should be updated
---------------------------------------------------------------------------------
SELECT A.id, A.symbol, A.name, A.industry, B.id, B.symbol, B.name, B.industry FROM StagingSecurities A
INNER JOIN StagingSecurities B ON B.symbol = A.symbol
WHERE A.id > B.id AND ( 
( A.name <> B.name )
OR ( A.industry IS NOT NULL AND B.industry IS NULL )
OR ( A.industry IS NOT NULL AND B.industry IS NULL )
)
LIMIT 5;

SELECT A.symbol, A.name, A.industry, B.symbol, B.name, B.industry FROM Securities A
INNER JOIN StagingSecurities B ON B.symbol = A.symbol
WHERE ( A.name <> B.name )
OR ( A.industry IS NOT NULL AND B.industry IS NULL )
OR ( A.sector IS NOT NULL AND B.sector IS NULL )
LIMIT 5;

UPDATE Securities A
INNER JOIN StagingSecurities B ON B.symbol = A.symbol
SET A.name = B.name, A.industry = B.industry, A.sector = B.sector
WHERE ( A.name <> B.name )
OR ( ( A.industry IS NULL AND B.industry IS NOT NULL ) OR A.industry <> B.industry )
OR ( ( A.sector IS NULL AND B.sector IS NOT NULL ) OR A.sector <> B.sector );




