USE shocktrade;

-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'ldaniels', 250000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'fugitive528', 250000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'gunst4rhero', 250000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'gadget', 250000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'daisy', 250000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'natech', 250000);

INSERT INTO user_awards (userAwardID, userID, awardCode) SELECT uuid(), U.userID, 'MADMONEY' FROM users U WHERE username = 'fugitive528';
INSERT INTO user_awards (userAwardID, userID, awardCode) SELECT uuid(), U.userID, 'CHKDFLAG' FROM users U WHERE username = 'fugitive528';
INSERT INTO user_awards (userAwardID, userID, awardCode) SELECT uuid(), U.userID, 'CRYSTBAL' FROM users U WHERE username = 'fugitive528';
INSERT INTO user_awards (userAwardID, userID, awardCode) SELECT uuid(), U.userID, 'GLDTRPHY' FROM users U WHERE username = 'fugitive528';
INSERT INTO user_awards (userAwardID, userID, awardCode) SELECT uuid(), U.userID, 'PAYDIRT' FROM users U WHERE username = 'fugitive528';

-- ------------------------------------------------------------
-- Posts
-- ------------------------------------------------------------
INSERT INTO posts (postID, userID, text, likes)
SELECT uuid(), userID, 'Winter is coming... Soon....', 0
FROM users
WHERE username = 'ldaniels528';

INSERT INTO post_tags (postID, userID, hashTag)
SELECT P.postID, U.userID, '#gameOfThrones'
FROM posts P
INNER JOIN users U ON U.userID = P.userID
WHERE U.username = 'ldaniels528';