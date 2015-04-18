db.Contests.find({ "$or" : [ { "perksAllowed" : true }, { "perksAllowed" : { "$exists" : false } } ] }, { name : 1 } );

db.Contests.find({ "perksAllowed" : true }, { name : 1 } );

db.Contests.update({ "name" : "test" }, { "$set" : { "perksAllowed" : true } } );
