db.users.aggregate(
    { $sort : { changePct : -1, posts: 1 } }
);

//////////////////////////////////////////////////////////////////
//	To return all states with a population greater than 10 million, 
//	use the following aggregation operation:
//////////////////////////////////////////////////////////////////

db.zipcodes.aggregate( { $group :
                         { _id : "$state",
                           totalPop : { $sum : "$pop" } } },
                       { $match : {totalPop : { $gte : 10*1000*1000 } } } );


db.Stocks.aggregate({ $group : { _id : "symbol", totalChange : { $sum : "ChangePct" }  } }).limit(10)
                       
                       
                    