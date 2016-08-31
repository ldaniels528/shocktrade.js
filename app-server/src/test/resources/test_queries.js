db.Contests.update({"name": "The World is Mine"},
    { $set : {
        "status": "INACTIVE",
        "creationTime": new ISODate(),
        "startTime": ISODate(),
        "expirationTime": ISODate("2017-08-02T17:48:36.350+0000") }
    });

db.Contests.findOne({"name": "The World is Mine too"});

db.Contests.findOne({
    "name": "The World is Mine too",
    "participants.positions" : { $elemMatch : { "symbol" : "AAPL", "quantity" : { $gte : 10 }}}}, { "participants.positions" : 1 });


db.Contests.findOne({"name": "The World is Mine too"}, {"status":1, "creationTime":1, "startTime":1, "expirationTime":1, "nextUpdate":1});


db.Contests.findOne({"status" : "ACTIVE", $or : [{ "nextUpdate" : {  $exists : false }}, { "nextUpdate" : { $lte : 1472362879570 } } ] });
