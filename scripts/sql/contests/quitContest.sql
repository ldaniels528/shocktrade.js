USE shocktrade;

DROP PROCEDURE IF EXISTS quitContest;
DELIMITER //
CREATE PROCEDURE quitContest(a_contestID VARCHAR(36), a_userID CHAR(36))
BEGIN
    -- turn off auto-commit, and start a new transaction
	SET autocommit = OFF;
    START TRANSACTION;

    -- remove the player from the game
    DELETE FROM portfolios P
    WHERE P.contestID = a_contestID
    AND P.userID = a_userID
    AND NOT EXISTS(SELECT portfolioID FROM portfolios WHERE userID = a_userID);

    -- if the player was removed ...
    IF ROW_COUNT() > 0 THEN

        -- reimburse the entry fee
        UPDATE users U
        INNER JOIN contests C ON C.contestID = a_contestID
        SET funds = funds + C.startingBalance
        WHERE userID = a_userID;

    END IF;

	-- commit and re-enable auto-commit
    SET autocommit = ON;

END //
DELIMITER ;
