db.Contests.update({name: "Girl Power"}, { $set :
{ creator : { "_id" : ObjectId("51a308ac50c70a97d375a6b8"), "name" : "seralovett", "facebookID" : "1589191728" }}});

db.Contests.update({name: "Girl Power"}, { $set :
{ "participants.$.name" : "seralovettt" } });





curl -i -H "Content-Type: application/json" -X POST -d '
{
	"_id" : { "$oid" : "5206fe9d84ae3b6ecab176b6" },
	"name" : "Top Gains",
	"conditions" : [ {
		"_id" : { "$oid" : "5206fe9d84ae3b6ecab176b5" },
		"field" : "VOLUME",
		"operator" : ">=",
		"value" : 1000000
	} ],
	"sortField" : "VOLUME",
	"maxResults" : 25,
	"exchangesToExclude" : [],
	"ascending" : false	
}' \
http://localhost:9000/api/quotes/filter


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
