USE shocktrade;

DROP PROCEDURE IF EXISTS joinContest;
DELIMITER //
CREATE PROCEDURE joinContest(a_contestID VARCHAR(36), a_userID CHAR(36))
BEGIN
	-- turn off auto-commit, and start a new transaction
	SET autocommit = OFF;
    START TRANSACTION;

    -- insert the player into the game
    INSERT INTO portfolios (portfolioID, contestID, userID, funds)
    SELECT uuid(), C.contestID, U.userID, C.startingBalance
    FROM users U, contests C
    WHERE C.contestID = a_contestID
    AND U.userID = a_userID
    LIMIT 1;

    -- if the player was removed ...
    IF ROW_COUNT() > 0 THEN

        -- deduct the entry fee
        UPDATE users U
        INNER JOIN contests C ON C.contestID = a_contestID
        SET wallet = wallet - C.startingBalance
        WHERE U.userID = a_userID;

    END IF;

	-- commit and re-enable auto-commit
    SET autocommit = ON;
    COMMIT;
END //
DELIMITER ;
