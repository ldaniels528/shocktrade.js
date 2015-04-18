db.Contests.remove({name:"King of the North"});
db.Contests.insert({
	"_id" : new ObjectId(),
	"name" : "King of the North",
	"participants" : [{
			"_id" : ObjectId("51a308ac50c70a97d375a6b2"),
			"name" : "ldaniels",
			"facebookID" : "1796471892",
			"fundsAvailable" : 25000,
			"score" : 0,
			"lastTradeTime" : new Date()
		},{
			"_id" : ObjectId("51b6a6a4ea2364b878e0701c"),
			"name" : "gadget",
			"facebookID" : "100002058615115",
			"fundsAvailable" : 25000,
			"score" : 0,
			"lastTradeTime" : new Date()
		},{
			"_id" : ObjectId("51a308ac50c70a97d375a6b8"),
			"name" : "seralovett",
			"facebookID" : "1589191728",
			"fundsAvailable" : 25000,
			"score" : 0,
			"lastTradeTime" : new Date()
		},{
			"_id" : ObjectId("51a308ac50c70a97d375a6b4"),
			"name" : "dizorganizer",
			"facebookID" : "100003027501772",
			"fundsAvailable" : 25000,
			"score" : 0,
			"lastTradeTime" : new Date()
		},{
			"_id" : ObjectId("51a308ac50c70a97d375a6b4"),
			"name": "natech", 
			"facebookID":"1377815655",
			"fundsAvailable" : 25000,
			"score" : 0,
			"lastTradeTime" : new Date()
		},{
			"_id" : ObjectId("51a308ac50c70a97d375a6b6"),
			"name": "gunstarhero", 
			"facebookID":"692041392",
			"fundsAvailable" : 25000,
			"score" : 0,
			"lastTradeTime" : new Date()
		}],
	"levelCap" : null,	
	"perksAllowed" : true,
	"ranked" : true,
	"playerCount" : 6,
	"processedTime" : new Date(),
	"startTime" : ISODate("2014-04-15T07:00:00Z"),
	"startingBalance" : 25000,
	"status" : "ACTIVE",	
	"creationTime" : new Date(),
	"creator" : "ldaniels",
	"expirationTime" : ISODate("2014-05-28T07:00:00Z"),
	"lastMarketClose" : ISODate("2014-03-17T04:04:10.637Z"),
	"lastUpdatedTime" : ISODate("2014-03-17T04:04:10.637Z"),
	"messages" : [{
			"_id" : new ObjectId(),
			"sentTime" : new Date(),
			"sender" : { "name": "gadget", "facebookID":"100002058615115" },
			"recipient" : null,
			"text" : "Hello"
		}]
});