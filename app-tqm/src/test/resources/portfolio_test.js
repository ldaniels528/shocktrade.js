db.Portfolios.update({_id: ObjectId("57c270fd207bc10ca05102dd")}, {
        contestID: "57c270fd207bc10ca05102dd",
        playerID: "557fc2895882c8421f3c5580",
        status: "ACTIVE",
        creationTime: new ISODate(),
        startTime: ISODate(),
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
        }, {
            _id: ObjectId().valueOf(),
            symbol: "AMD",
            accountType: "CASH",
            orderType: "SELL",
            priceType: "LIMIT",
            price: 7.0,
            quantity: 500,
            commission: 9.99,
            creationTime: 1472252400000
        }]
    },
    {upsert: true});