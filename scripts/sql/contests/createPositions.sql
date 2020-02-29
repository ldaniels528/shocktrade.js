USE shocktrade;

DELIMITER //
DROP PROCEDURE IF EXISTS createPositions;
CREATE PROCEDURE createPositions(effectiveTime DATETIME)
BEGIN

    -- ---------------------------------------------------------------------------
    --      BUY ORDERS
    -- ---------------------------------------------------------------------------

    -- close orders with insufficient funds
    UPDATE orders O
    INNER JOIN users U ON U.userID = O.userID
    INNER JOIN stocks S ON S.symbol = O.symbol
    SET
        O.closed = 1,
        O.processedTime = now(),
        O.message = 'Insufficient funds'
    WHERE O.closed = 0
    AND O.orderType = 'BUY'
    AND U.funds < S.lastSale * O.quantity
    AND S.tradeDateTime BETWEEN O.creationTime AND O.expirationTime
    AND (
        O.priceType = 'MARKET'
        OR O.priceType = 'MARKET_AT_CLOSE'
        OR (O.priceType = 'LIMIT' AND O.price >= S.lastSale)
    );

    -- fulfill BUY orders
    INSERT INTO positions (positionID, orderID, userID, symbol, exchange, price, quantity, tradeDateTime, processedTime)
    SELECT uuid(), O.orderID, O.userID, O.symbol, O.exchange, S.lastSale, O.quantity, S.tradeDateTime, now()
    FROM orders O
    INNER JOIN users U ON U.userID = O.userID
    INNER JOIN stocks S ON S.symbol = O.symbol
    WHERE O.closed = 0
    AND O.orderType = 'BUY'
    AND U.funds >= S.lastSale * O.quantity
    AND S.tradeDateTime BETWEEN O.creationTime AND O.expirationTime
    AND (
        O.priceType = 'MARKET'
        OR O.priceType = 'MARKET_AT_CLOSE'
        OR (O.priceType = 'LIMIT' AND O.price >= S.lastSale)
    );

    -- deduct the user's funds and close fulfilled BUY order(s)
    UPDATE users U
    INNER JOIN orders O ON O.userID = U.userID
    INNER JOIN positions P ON P.orderID = O.orderID
    INNER JOIN stocks S ON S.symbol = O.symbol
    SET
        O.closed = 1,
        O.fulfilled = 1,
        O.processedTime = P.processedTime,
        U.funds = U.funds - S.lastSale * O.quantity
    WHERE O.closed = 0
    AND O.orderType = 'BUY';

    -- ---------------------------------------------------------------------------
    --      SELL ORDERS
    -- ---------------------------------------------------------------------------

    UPDATE positions P
    INNER JOIN orders O ON O.userID = P.userID AND O.symbol = P.symbol
    INNER JOIN stocks S ON S.symbol = O.symbol
    INNER JOIN users U ON U.userID = O.userID
    SET
        O.closed = 1,
        O.fulfilled = 1,
        O.processedTime = now(),
        P.quantity = P.quantity - O.quantity,
        U.funds = U.funds + S.lastSale * O.quantity
    WHERE O.orderType = 'SELL'
    AND P.quantity >= O.quantity
    AND O.closed = 0
    AND (
        O.priceType = 'MARKET'
        OR O.priceType = 'MARKET_AT_CLOSE'
        OR (O.priceType = 'LIMIT' AND S.lastSale >= O.price)
    );

END //
DELIMITER ;
