USE shocktrade;

-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'ldaniels', 125000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'fugitive528', 125000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'gunst4rhero', 125000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'gadget', 125000);
INSERT INTO users (userID, username, wallet) VALUES (uuid(), 'daisy', 125000);

INSERT INTO user_awards (userAwardID, userID, awardCode) VALUES (uuid(), '19522944-6b4d-11ea-83ac-857ee918b853', 'SOCLITE')
SELECT uuid(), U.userID, 'SOCLITE' FROM users WHERE username = 'fugitive528';

INSERT INTO user_awards (userAwardID, userID, awardCode)
SELECT uuid(), U.userID, 'PERKS' FROM users WHERE username = 'fugitive528';

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