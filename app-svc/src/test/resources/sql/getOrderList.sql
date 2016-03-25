CREATE PROCEDURE getOrderList ( userId: BIGINT )
    RETURN RESULTSET IS
BEGIN

SELECT o.* 
FROM Orders o, UserSimulations us, Users u
WHERE o.simulationId = us.simulationId
AND us.userId = u.id
AND u.id = userId;

END getOrderList;