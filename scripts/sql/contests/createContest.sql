USE shocktrade;

DROP PROCEDURE IF EXISTS createContest;
DELIMITER //
CREATE PROCEDURE createContest(
    a_name VARCHAR(128),
    a_creatorUserID CHAR(36),
    a_startingBalance DECIMAL(12,5),
    a_startAutomatically TINYINT,
    a_duration INTEGER,
    a_friendsOnly TINYINT,
    a_invitationOnly TINYINT,
    a_levelCap INTEGER,
    a_perksAllowed TINYINT,
    a_robotsAllowed TINYINT
)
BEGIN
    DECLARE v_contestID CHAR(36);
    DECLARE v_portfolioID CHAR(36);

	-- turn off auto-commit, and start a new transaction
	SET autocommit = OFF;
    START TRANSACTION;
    
    -- ---------------------------------------------------------------------------
    --  deduct the entry fee
    -- ---------------------------------------------------------------------------
    UPDATE users
    SET wallet = wallet - a_startingBalance
    WHERE userID = a_creatorUserID
    AND wallet - a_startingBalance >= 0;

    -- if the funds were deducted
    IF ROW_COUNT() > 0 THEN
        -- ---------------------------------------------------------------------------
        --  create the contest
        -- ---------------------------------------------------------------------------
        SET v_contestID = uuid();
        INSERT INTO contests (
            contestID, hostUserID, name, startingBalance, expirationTime,
            friendsOnly, invitationOnly, levelCap, perksAllowed, robotsAllowed
        )
        SELECT v_contestID, U.userID, a_name, a_startingBalance, DATE_ADD(now(), INTERVAL a_duration DAY),
               a_friendsOnly, a_invitationOnly, a_levelCap, a_perksAllowed, a_robotsAllowed
        FROM users U
        WHERE U.userID = a_creatorUserID;

        -- ---------------------------------------------------------------------------
        --  create the player's portfolios
        -- ---------------------------------------------------------------------------
        SET v_portfolioID = uuid();
        INSERT INTO portfolios (portfolioID, userID, contestID, funds)
        SELECT v_portfolioID, C.hostUserID, C.contestID, C.startingBalance
        FROM contests C
        WHERE C.contestID = v_contestID;

        -- ---------------------------------------------------------------------------
        --  create a welcome message
        -- ---------------------------------------------------------------------------
        INSERT INTO contest_chats (messageID, contestID, userID, message)
        SELECT uuid(), P.contestID, P.userID, CONCAT("Welcome to ", a_name, "!")
        FROM portfolios P
        WHERE P.contestID = v_contestID;

        -- ---------------------------------------------------------------------------
        --  Finally, return the contest and portfolio IDs
        -- ---------------------------------------------------------------------------
        SELECT v_contestID, v_portfolioID;
        COMMIT;
    ELSE
        SELECT NULL, NULL;
        ROLLBACK;
    END IF;

	-- commit and re-enable auto-commit
    SET autocommit = ON;

END //
DELIMITER ;
