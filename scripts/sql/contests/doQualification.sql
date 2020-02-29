USE shocktrade;

DROP PROCEDURE IF EXISTS doQualification;
DELIMITER //
CREATE PROCEDURE doQualification()
BEGIN
    -- declare the local variables
    DECLARE done INTEGER DEFAULT 0;
    DECLARE v_portfolioID CHAR(36);
    DECLARE v_orderID CHAR(36);
    DECLARE v_userID CHAR(36);
    DECLARE v_symbol VARCHAR(12);
    DECLARE v_exchange VARCHAR(12);
    DECLARE v_price DOUBLE;
    DECLARE v_quantity INTEGER;
    DECLARE v_tradeDateTime DATETIME;
    DECLARE v_cost DOUBLE;
    DECLARE row_count INTEGER;
    DECLARE total_count INTEGER DEFAULT 0;
    
    -- declare the cursor
    DECLARE cursor1 CURSOR FOR
        SELECT userID, portfolioID, orderID, symbol, exchange, price, quantity, cost, tradeDateTime
        FROM qualifications;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    
    OPEN cursor1;

    -- use the cursor to process each order
    cqm_loop: LOOP
        -- fetch a qualification record
        FETCH cursor1 INTO v_userID, v_portfolioID, v_orderID, v_symbol, v_exchange, v_price, v_quantity, v_cost, v_tradeDateTime;

        -- are all records processed?
        IF done THEN 
            CLOSE cursor1;
			LEAVE cqm_loop;
		END IF;	

        -- deduct the funds
        UPDATE portfolios SET funds = funds - v_cost
        WHERE portfolioID = v_portfolioID
        AND funds - v_cost >= 0;

        -- if the funds were deducted, proceed.
        SET row_count = ROW_COUNT();
        SET total_count = total_count + row_count;
        IF row_count > 0 THEN
            -- create the position
            INSERT INTO positions (positionID, portfolioID, orderID, symbol, exchange, price, quantity, tradeDateTime)
            SELECT uuid(), v_portfolioID, v_orderID, v_symbol, v_exchange, v_price, v_quantity, v_tradeDateTime;

            -- close the order
            UPDATE orders SET processedTime = now(), closed = TRUE, fulfilled = TRUE WHERE orderID = v_orderID;
        END IF;

    END LOOP;

    SELECT total_count;

END //
;
DELIMITER ;