INSERT INTO Users ( name, password ) VALUES ( 'ldaniels', '123456' );
INSERT INTO Users ( name, password ) VALUES ( 'erv970', 'sugar' );
INSERT INTO Users ( name, password ) VALUES ( 'natech', 'halo1' );
INSERT INTO Users ( name, password ) VALUES ( 'natech', 'halo1' );

INSERT INTO Users ( name, password ) VALUES ( 'atom', '!tr@adeFr33ly' );

SELECT u.userId, u.name, t.loginTime, a.logoutTime
FROM Users u
INNER JOIN UserAccess a ON a.userId = u.userId
INNER JOIN ( SELECT userId, MAX(loginTime) AS loginTime FROM UserAccess GROUP BY userId ) AS t ON t.userId = u.userId
WHERE a.loginTime = t.loginTime
ORDER BY a.loginTime DESC;

-- get the user activity
SELECT id, userId, userSessionId, accessTime, resource, responseTimeMillis, errorMessage FROM UserActivities WHERE userSessionId = 2118 ORDER BY id DESC;



ATOM
Autonomous Trading & Options Manager