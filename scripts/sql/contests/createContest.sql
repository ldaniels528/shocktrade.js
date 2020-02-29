USE shocktrade;

DELIMITER //
DROP PROCEDURE IF EXISTS createContest;
CREATE PROCEDURE createContest(
    name VARCHAR(128),
    creatorUserID CHAR(36),
    status VARCHAR(12),
    startingBalance DECIMAL(12,5),
    friendsOnly BIT,
    invitationOnly BIT,
    levelCap BIT,
    perksAllowed BIT,
    robotsAllowed BIT
)
BEGIN

    -- ---------------------------------------------------------------------------
    --  First, create the contest
    -- ---------------------------------------------------------------------------
    INSERT INTO contests (contestID, name, startingBalance, startTime, expirationTime)
    VALUES (uuid(), name, startingBalance, now(), ?, ?)

END //
DELIMITER ;
