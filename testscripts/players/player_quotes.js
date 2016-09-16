
db.Players.update({name:"ldaniels"}, { $set: { favorites : [ "AAPL", "AMD", "AMZN", "SNE", "GOOG", "INTC", "MSFT" ] } })


db.Players.update({name:"ldaniels"}, { $set: { filters : [
	{
		"_id" : new ObjectId(),
		"name" : "Top Gains",
		"headers" : [ "Symbol", "Price", "Change", "Spread", "Volume" ],
		"columns" : [ "symbol", "lastTrade", "changePct", "spread", "volume" ],
		"conditions" : [ {
			"_id" : new ObjectId(),
			"field" : "CHANGEPCT",
			"operator" : ">=",
			"value" : 25
		} ],
		"sortField" : "CHANGEPCT",
		"maxResults" : 25,
		"exchangesToExclude" : [],
		"ascending" : false	
	},
	{
		"_id" : new ObjectId(),
		"name" : "Top Losses",
		"headers" : [ "Symbol", "Price", "Change", "Spread", "Volume" ],
		"columns" : [ "symbol", "lastTrade", "changePct", "spread", "volume" ],
		"conditions" : [ {
			"_id" : new ObjectId(),
			"field" : "CHANGEPCT",
			"operator" : "<",
			"value" : 25
		} ],
		"sortField" : "CHANGEPCT",
		"maxResults" : 25,
		"exchangesToExclude" : [],
		"ascending" : true	
	},
	{
		"_id" : new ObjectId(),
		"name" : "Most Active",
		"headers" : [ "Symbol", "Price", "Change", "Spread", "Volume" ],
		"columns" : [ "symbol", "lastTrade", "changePct", "spread", "volume" ],
		"conditions" : [ {
			"_id" : new ObjectId(),
			"field" : "VOLUME",
			"operator" : ">=",
			"value" : 1000000
		} ],
		"sortField" : "VOLUME",
		"maxResults" : 25,
		"exchangesToExclude" : [],
		"ascending" : false	
	},	
]} });
