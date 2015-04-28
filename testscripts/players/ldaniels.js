db.Players.remove({
	name : "ldaniels"
});
db.Players.insert({
	"_id" : ObjectId("51a308ac50c70a97d375a6b2"),
	"acquaintances" : [ "naughtymonkey" ],
	"awards" : [ "MADMONEY", "CHKDFLAG", "FACEBOOK", "PERK" ],
	"country" : "us",
	"email" : "lawrence.daniels@gmail.com",
	"facebookID" : "1796471892",
	"favorites" : [ "ROSV", "JYHW", "DKAM", "STKO", "MWIP", "UGNE", "AHFD",
			"ELAY", "GYST", "AMEL", "ISCO", "LPR", "PGSY", "NEOM", "ECOS",
			"TVER", "AMD", "GOOG", "TWTR" ],
	"filters" : [ {
		"_id" : ObjectId("5206fe9d84ae3b6ecab176b4"),
		"name" : "Most Active",
		"headers" : [ "Symbol", "Last", "Change %", "Volume" ],
		"columns" : [ "symbol", "lastTrade", "changePct", "volume" ],		
		"ascending" : false,
		"conditions" : [ {
			"_id" : ObjectId("5206fe9d84ae3b6ecab176b3"),
			"field" : "VOLUME",
			"operator" : ">=",
			"value" : NumberLong(1000000)
		}, {
			"_id" : ObjectId("52093f5d84ae3a2e05423ca4"),
			"field" : "SPREAD",
			"operator" : ">=",
			"value" : 25
		} ],
		"exchangesToExclude" : [],
		"maxResults" : 25,
		"sortField" : "VOLUME"
	}, {
		"_id" : ObjectId("5206fe9d84ae3b6ecab176b6"),
		"name" : "Top Gains",
		"headers" : [ "Symbol", "Last", "Change %", "Volume" ],
		"columns" : [ "symbol", "lastTrade", "changePct", "volume" ],
		"conditions" : [ {
			"_id" : ObjectId("5206fe9d84ae3b6ecab176b5"),
			"field" : "CHANGE",
			"operator" : ">=",
			"value" : 25
		} ],
		"sortField" : "CHANGE",
		"maxResults" : 25,
		"exchangesToExclude" : [],
		"ascending" : false
	}, {
		"_id" : ObjectId("5206fe9d84ae3b6ecab176b8"),
		"name" : "Top Losses",
		"headers" : [ "Symbol", "Last", "Change %", "Volume" ],
		"columns" : [ "symbol", "lastTrade", "changePct", "volume" ],
		"conditions" : [ {
			"_id" : ObjectId("5206fe9d84ae3b6ecab176b7"),
			"field" : "CHANGE",
			"operator" : "<=",
			"value" : -25
		} ],
		"sortField" : "CHANGE",
		"maxResults" : 25,
		"exchangesToExclude" : [],
		"ascending" : true
	}, {
		"_id" : ObjectId("5206fe9d84ae3b6ecab176ba"),
		"name" : "Top Spread",
		"headers" : [ "Symbol", "Last", "Change %", "Spread %", "Volume" ],
		"columns" : [ "symbol", "lastTrade", "changePct", "spread", "volume" ],
		"ascending" : true,
		"conditions" : [ {
			"_id" : ObjectId("5206fe9d84ae3b6ecab176b9"),
			"field" : "SPREAD",
			"operator" : ">=",
			"value" : 25
		}, {
			"_id" : ObjectId("520d2b20e4b090762ee50086"),
			"field" : "CHANGE",
			"operator" : "<",
			"value" : 0
		} ],
		"exchangesToExclude" : [],
		"maxResults" : 25,
		"sortField" : "CHANGE"
	}, {
		"_id" : ObjectId("52093d0c84ae3a2e05423c74"),
		"name" : "Buying Opportunities",
		"headers" : [ "Symbol", "Last", "Change %", "Volume" ],
		"columns" : [ "symbol", "lastTrade", "changePct", "volume" ],
		"ascending" : true,
		"conditions" : [ {
			"_id" : ObjectId("52093dce84ae3a2e05423c94"),
			"field" : "LAST_TRADE",
			"operator" : ">=",
			"value" : 0.01
		}, {
			"_id" : ObjectId("520b0e7ee4b090762ee5001a"),
			"field" : "SPREAD",
			"operator" : ">=",
			"value" : 25
		}, {
			"_id" : ObjectId("520b0e98e4b090762ee50022"),
			"field" : "VOLUME",
			"operator" : ">=",
			"value" : NumberLong(100000)
		}, {
			"_id" : ObjectId("5253906184ae6f60939a5faa"),
			"field" : "CHANGE",
			"operator" : "<",
			"value" : 0
		} ],
		"exchangesToExclude" : [],
		"maxResults" : 25,
		"sortField" : "CHANGE"
	}, {
		"_id" : ObjectId("52604f7f84ae9a83d0b4e11f"),
		"name" : "Penny Stocks",
		"headers" : [ "Symbol", "Last", "Change %", "Volume" ],
		"columns" : [ "symbol", "lastTrade", "changePct", "volume" ],
		"ascending" : true,
		"conditions" : [ {
			"_id" : ObjectId("52604f9e84ae9a83d0b4e120"),
			"field" : "LAST_TRADE",
			"operator" : "<=",
			"value" : 0.05
		}, {
			"_id" : ObjectId("52604fb084ae9a83d0b4e121"),
			"field" : "VOLUME",
			"operator" : ">",
			"value" : NumberLong(1000000)
		}, {
			"_id" : ObjectId("5260505a84ae9a83d0b4e122"),
			"field" : "SPREAD",
			"operator" : ">=",
			"value" : 15
		} ],
		"exchangesToExclude" : [],
		"maxResults" : 25,
		"sortField" : "CHANGE"
	} ],
	"friends" : [ "brooklynn", "dizorganizer", "erv970", "natech", "pdavis77",
			"seralovett", "shanc37", "dangphuongthuy", "elizabeth",
			"mfwilliams", "rgoodall", "raeci", "sugarmomma", "mrbrooks",
			"bjgrey", "gunstarhero", "tricky", "gadget" ],
	"gamesCompleted" : 2,
	"gamesCreated" : 3,
	"gamesDeleted" : 1,
	"lastLoginTime" : ISODate("2014-03-01T03:32:41.513Z"),
	"level" : 3,
	"loginTime" : ISODate("2013-06-14T23:56:43.748Z"),
	"modules" : [ "MKT_WATCH" ],
	"netCashEarned" : 29000.57,
	"netWorth" : 250000.00,
	"perks" : [ "CREATOR" ],
	"recentSymbols" : ['AAPL', 'AMZN', 'GOOG', 'MSFT'],
	"rep" : 1,
	"totalXP" : 902
});