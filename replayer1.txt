# First lookup the user
[a] findUserByName fugitive528
print "findUserByName: $$a"

# create a new game
[b] createNewGame { "name":"It's the new Style!", "userID":"$$a.userID", "startingBalance":2500, "duration":3, "perksAllowed":true, "robotsAllowed":false }
print "createNewGame: $$b"

# send a chat message to the contestants
[c] putChatMessage $$b.contestID { "userID":"$$a.userID", "username":"$$a.username", "message":"This is gonna be awesome!" }
print "putChatMessage: $$c"
