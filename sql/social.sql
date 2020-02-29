-- ------------------------------------------------------------
-- Posts
-- ------------------------------------------------------------

DROP TABLE IF EXISTS posts;
CREATE TABLE posts (
    postID CHAR(36) NOT NULL PRIMARY KEY,
    userID CHAR(36) NOT NULL,
    text TEXT NOT NULL,
    likes INTEGER NOT NULL DEFAULT 0,
    creationTime DATETIME NOT NULL DEFAULT now(),
    lastModifiedTime DATETIME NOT NULL DEFAULT now()
);

DROP TABLE IF EXISTS post_tags;
CREATE TABLE post_tags (
    uid INTEGER AUTO_INCREMENT PRIMARY KEY,
    postID CHAR(36) NOT NULL,
    userID CHAR(36) NOT NULL,
    hashTag VARCHAR(32) NOT NULL
);