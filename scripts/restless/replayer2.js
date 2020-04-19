// First lookup the user
[u] findUserByName teddy

// search for games
[c] contestSearch { "userID":"$$u.userID", "myGamesOnly":true }

// send a chat message to the contestants
[cc] arrayGet 0 $$c
print "entry fee:\t $$cc.startingBalance"
print "contest name:\t $$cc.name"
print "contest ID:\t $$cc.contestID"

debug on
[xx] put http://localhost:9000/api/contests/search { "userID":"$$u.userID", "myGamesOnly":true }
[yy] arrayLength $$xx
print $$yy
debug off