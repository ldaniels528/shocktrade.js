USE shocktrade;

DROP PROCEDURE IF EXISTS joinContest;
DELIMITER //
CREATE PROCEDURE joinContest(a_contestID VARCHAR(36), a_userID CHAR(36))
BEGIN
	-- turn off auto-commit, and start a new transaction
	SET autocommit = OFF;
    START TRANSACTION;

    -- insert the player into the game
    INSERT INTO portfolios (contestID, userID, funds)
    SELECT C.contestID, U.userID, C.startingBalance
    FROM contests C
    INNER JOIN users U ON U.userID = C.userID
    WHERE C.contestID = a_contestID
    AND U.userID = a_userID;

    -- if the player was removed ...
    IF ROW_COUNT() > 0 THEN

        -- deduct the entry fee
        UPDATE users U
        INNER JOIN contests C ON C.userID = U.userID
        SET funds = funds - C.startingBalance
        WHERE U.userID = a_userID;

    END IF;

	-- commit and re-enable auto-commit
    SET autocommit = ON;

END //
DELIMITER ;
