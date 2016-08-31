db.Contests.update({name: "The World is Mine too"}, {
        name: "The World is Mine too",
        status: "ACTIVE",
        creationTime: new ISODate(),
        startTime: ISODate(),
        expirationTime: ISODate("2017-08-02T17:48:36.350+0000"),
        participants: [{
            _id: "557fc2895882c8421f3c5580",
            name: "Fugitive528",
            facebookID: "1796471892",
            cashAccount: {
                cashFunds: 25000,
                asOfDate: new ISODate()
            },
            orders: [{
                _id: ObjectId().valueOf(),
                symbol: "AAPL",
                accountType: "CASH",
                orderType: "BUY",
                priceType: "LIMIT",
                price: 400,
                quantity: 10,
                commission: 9.99,
                creationTime: 1472252400000
            }, {
                _id: ObjectId().valueOf(),
                symbol: "AMD",
                accountType: "CASH",
                orderType: "BUY",
                priceType: "LIMIT",
                price: 8.0,
                quantity: 1000,
                commission: 9.99,
                creationTime: 1472252400000
            }]
        }]
    },
    {upsert: true});


db.Contests.update({
    name: "The World is Mine too",
    "participants._id" : "557fc2895882c8421f3c5580"
}, {
    $addToSet: {
        "participants.$.orders": {
            _id: ObjectId().valueOf(),
            symbol: "AMD",
            accountType: "CASH",
            orderType: "SELL",
            priceType: "LIMIT",
            price: 7.0,
            quantity: 500,
            commission: 9.99,
            creationTime: 1472252400000
        }
    }
});