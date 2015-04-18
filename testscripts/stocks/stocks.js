db.Stocks.find({industry:"Unclassified"}).forEach(function(elem) {
	db.Stocks.update({ _id : elem._id }, { $set : { industry : elem.sector } });
});

db.Stocks.find({subIndustry:"Unclassified"}).forEach(function(elem) {
	db.Stocks.update({ _id : elem._id }, { $set : { subIndustry : elem.industry } });
});


db.Stocks.aggregate([
    { $project : { exchange : 1, securities : 1 } },
	{ $group : { _id : "$exchange", securities : { $sum : 1 } } }, 
	{ $sort : { securities : -1 } },
	{ $match : { assetType : { $in : ["Common Stock","ETF"] } } }
]);


db.Stocks.aggregate([
	{ $match : { assetType : { $in : ["Common Stock","ETF"] } } },
 	{ $group : { _id : "$exchange", total : { $sum : 1 } } }, 
 	{ $sort : { total : -1 } }
]);


db.Stocks.find({ exchange : null, exchangeCategory : { $ne : null }}).forEach(function(elem) {
	db.Stocks.update({ _id : elem._id }, { $set : { exchange : elem.exchangeCategory } });
});


db.Stocks.find({}, {symbol:1, exchange:1});

db.Stocks.update({exchange:"OTC BB"}, {$set:{exchange : "OTCBB"}}, {multi:true});


db.zipcodes.aggregate({
	$group : {
		_id : "$state",
		totalPop : {
			$sum : "$pop"
		}
	}
}, {
	$match : {
		totalPop : {
			$gte : 10 * 1000 * 1000
		}
	}
})


cfg.members = [{"_id" : 0, "host"  : "192.168.1.5"}]
