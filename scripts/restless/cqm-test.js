// First lookup the users
print "looking up users..."
[fugitive528] findUserByName fugitive528
[gadget] findUserByName gadget
[teddy] findUserByName teddy
[daisy] findUserByName daisy
[joey] findUserByName joey
[naughtymonkey] findUserByName naughtymonkey
[natech] findUserByName natech
[gunst4rhero] findUserByName gunst4rhero
[seralovett] findUserByName seralovett
[dizorganizer] findUserByName dizorganizer
[dannywoo] findUserByName dannywoo
[ldaniels] findUserByName ldaniels
[chunkymonkey] findUserByName chunkymonkey

// create a new game
print "creating a new game..."
[contest] createContest { "name": "Sons of Israel", "userID": "$$fugitive528.userID", "startingBalance": 25000, "duration": 3 }

// join the other players to the contest
print "joining users to game..."
[join_gadget] joinContest $$contest.contestID $$gadget.userID
[join_teddy] joinContest $$contest.contestID $$teddy.userID
[join_daisy] joinContest $$contest.contestID $$daisy.userID
[join_joey] joinContest $$contest.contestID $$joey.userID
[join_naughtymonkey] joinContest $$contest.contestID $$naughtymonkey.userID
//[join_natech] joinContest $$contest.contestID $$natech.userID
//[join_gunst4rhero] joinContest $$contest.contestID $$gunst4rhero.userID
[join_seralovett] joinContest $$contest.contestID $$seralovett.userID
//[join_dizorganizer] joinContest $$contest.contestID $$dizorganizer.userID
[join_dannywoo] joinContest $$contest.contestID $$dannywoo.userID
[join_ldaniels] joinContest $$contest.contestID $$ldaniels.userID
[join_chunkymonkey] joinContest $$contest.contestID $$chunkymonkey.userID

// buy some securities
print "buying some securities..."
//[order1] createOrder $$contest.contestID $$fugitive528.userID { "symbol":"NXNN", "exchange":"OTCBB", "quantity":60000, "orderType":"BUY", "priceType":"LIMIT", "price":0.001, "orderTerm":"3" }
//[order2] createOrder $$contest.contestID $$gadget.userID { "symbol":"AAPL", "exchange":"NASDAQ", "quantity":25, "orderType":"BUY", "priceType":"MARKET", "orderTerm":"3" }
//[order3] createOrder $$contest.contestID $$teddy.userID { "symbol":"INTC", "exchange":"NASDAQ", "quantity":300, "orderType":"BUY", "priceType":"MARKET", "orderTerm":"3" }

// sell some securities
//print "selling some securities..."
//[order4] createOrder $$contest.contestID $$fugitive528.userID { "symbol":"AMD", "exchange":"NASDAQ", "quantity":600, "orderType":"SELL", "priceType":"MARKET", "orderTerm":"3" }
//print $$order4