

db.Contests.update(
    {name:"Claiming Testing", "participants.name":"ldaniels"},
    {
        "$pull" : { "participants.$.positions_TEMP" : {} }
    }
);



db.Contests.find({ "$or" : [ { "perksAllowed" : true }, { "perksAllowed" : { "$exists" : false } } ] }, { name : 1 } );

db.Contests.find({ "perksAllowed" : true }, { name : 1 } );

db.Contests.update({ "name" : "test" }, { "$set" : { "perksAllowed" : true } } );

db.Contests.findOne({name: "Trading Test"});

db.Contests.findOne({name: "Trading Test", "participants.orders" : { "$elemMatch" : { "_id" : ObjectId("554452e05dd0bcc90341c17a") }}});

db.Contests.update(
    { "name": "Trading Test", "participants.name" : "ldaniels"}, {
        "$pull": { "participants.$.orders" : { _id : ObjectId("554452e05dd0bcc90341c17a") } }
    });

db.Contests.update(
    { "name": "Trading Test", "participants.name" : "ldaniels"}, {
        "$addToSet": { "participants.$.orders" : {
            "_id" : ObjectId("554452e05dd0bcc90341c17a"),
            "symbol" : "AMD",
            "exchange" : "NasdaqCM",
            "creationTime" : ISODate("2015-05-01T04:30:24.352Z"),
            "orderType" : "BUY",
            "price" : 2.31,
            "priceType" : "MARKET",
            "quantity" : 1000,
            "commission" : 9.99,
            "emailNotify" : true,
            "volumeAtOrderTime" : NumberLong(11662803)
        }}
    });


db.Contests.update({
    name: "Me vs Gadget",
    "participants.name": "gadget"
}, {
    "$set": { "participants.$.closedOrders": [] }
});

db.Contests.update({
    name: "Me vs Gadget",
    "participants.name": "Fugitive528"
}, {
    "$set": {
        "participants.$.marginAccount.interestPaid" : 0.0,
        "participants.$.marginAccount.interestPaidAsOfDate": ISODate("2015-07-14T04:30:24.352Z")
    }
});

db.Contests.update({
    name: "The World is Mine",
    "participants.name": "Fugitive528"
}, {
    "$set": {
        "participants.$.marginAccount.interestPaid" : 0.0,
        "participants.$.marginAccount.interestPaidAsOfDate": ISODate("2015-07-14T04:30:24.352Z")
    }
});

db.Contests.update({
    name: "Me vs Gadget",
    "participants.name": "Fugitive528"
}, {
    "$inc": {
        "participants.$.marginAccount.cashFunds" : 1469.71
    }
});


db.Contests.findOne({name:"Me vs Gadget"}, {"participants.marginAccount":1});
db.Contests.findOne({name:"The World is Mine"}, {"participants.marginAccount":1});
