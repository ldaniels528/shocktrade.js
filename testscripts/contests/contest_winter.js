db.Contests.remove({
	name : "Winter is coming"
});
db.Contests.insert({
	"_id" : new ObjectId(),
	"name" : "Winter is coming",
	"participants" : [ {
		"_id" : ObjectId("51a308ac50c70a97d375a6b2"),
		"name" : "ldaniels",
		"facebookID" : "1796471892",
		"fundsAvailable" : 25000,
		"score" : 0,
		"lastTradeTime" : new Date(),
		"orders" : [ {
			_id : new ObjectId(),
			orderType : "BUY",
			symbol : "PLUG",
			exchange : "NASDAQ",
			priceType : "MARKET_AT_CLOSE",
			quantity : 100,
			volumeAtOrderTime : 0,
			commission : 6.99,
			orderTime : new Date(),
			creationTime: new Date(),
			expirationTime : ISODate("2014-04-31")
		}, {
			_id : new ObjectId(),
			orderType : "BUY",
			symbol : "AMD",
			exchange : "NYSE",
			priceType : "MARKET",
			quantity : 100,
			volumeAtOrderTime : 0,
			commission : 9.99,
			orderTime : new Date(),
			creationTime: new Date(),
			expirationTime : ISODate("2014-04-31")
		} ]
	}, {
		"_id" : ObjectId("51b6a6a4ea2364b878e0701c"),
		"name" : "gadget",
		"facebookID" : "100002058615115",
		"fundsAvailable" : 25000,
		"score" : 0,
		"lastTradeTime" : new Date()
	} ],
	"levelCap" : 4,
	"perksAllowed" : true,
	"ranked" : true,
	"playerCount" : 2,
	"processedTime" : new Date(),
	"startTime" : ISODate("2014-04-15T07:00:00Z"),
	"startingBalance" : 25000,
	"status" : "ACTIVE",
	"creationTime" : new Date(),
	"creator" : "ldaniels",
	"expirationTime" : ISODate("2014-05-28T07:00:00Z"),
	"lastMarketClose" : ISODate("2014-03-17T04:04:10.637Z"),
	"lastUpdatedTime" : ISODate("2014-03-17T04:04:10.637Z"),
	"messages" : [ {
		"_id" : new ObjectId(),
		"sentTime" : new Date(),
		"sender" : {
			"name" : "gadget",
			"facebookID" : "100002058615115"
		},
		"text" : "Let's do this! :-D"
	} ]
});