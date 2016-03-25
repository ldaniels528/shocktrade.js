SELECT COUNT(*)
FROM Messages M
INNER JOIN Users U ON U.id = M.recipientId
WHERE M.creationTime > '2012-03-20 11:00:00';