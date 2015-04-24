db.Contests.remove({
	name : "Girl Power"
});
db.Contests.insert({
	"_id" : new ObjectId(),
	"name" : "Girl Power",
	"creator" : "seralovett",
	"status" : "ACTIVE",
	"creationTime" : new Date(),
	"maxParticipants": 2,
	"playerCount" : 2,
	"processedTime" : new Date(),
	"startTime" : ISODate("2014-05-19T07:00:00Z"),
	"startingBalance" : 5000,
	"expirationTime" : ISODate("2015-05-31T07:00:00Z"),
	"levelCap" : 1,
	"perksAllowed" : false,
	"participants" : [ {
		"_id" : ObjectId("51a308ac50c70a97d375a6b8"),
		"name" : "seralovett",
		"facebookID" : "1589191728",
		"fundsAvailable" : 4210.01,
		"score" : 100,
		"lastTradeTime" : new Date(),
		"orders": [],
		"performance": [],
		"positions" : [ {
			"_id" : ObjectId("52557802e4b00a139f5d6ed6"),
			"symbol" : "PRGN",
			"exchange" : "NASDAQ",
			"price" : 7.80,
			"quantity" : 100,
			"commission" : 9.99,
			"cost" : 789.99,
			"processedTime" : ISODate("2013-10-10T20:15:27.194Z")
		} ]
	}, {
		"_id" : ObjectId("51a308ac50c70a97d375a6b2"),
		"name" : "ldaniels",
		"facebookID" : "1796471892",
		"fundsAvailable" : 5490.01,
		"score" : 100,
		"lastTradeTime" : new Date(),
		"orders": [],
		"performance": [],
		"positions" : [{
			"_id" : ObjectId("52557802e4b00a139f5d6ed7"),
			"symbol" : "AMD",
			"exchange" : "NYSE",
			"price" : 3.51,
			"quantity" : 1000,
			"commission" : 9.99,
			"cost" : 3519.99,
			"processedTime" : ISODate("2014-01-10T20:15:27.194Z")
		} ]
	} ],
	"messages" : [ {
		"_id" : new ObjectId(),
		"sentTime" : new Date(),
		"sender" : {
			"name" : "seralovett",
			"facebookID" : "1589191728"
		},
		"recipient" : null,
		"text" : "Welcome to Girl Power!"
	} ]
});