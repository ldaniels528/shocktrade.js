db.Portfolios.drop();

db.Portfolios.update({_id: ObjectId("57e2e2a3268dec60af793bfa")}, {
        contestID: "57d3472bfee1c54362297f97",
        playerID: "51a308ac50c70a97d375a6b5",
        status: "ACTIVE",
        creationTime: new ISODate(),
        cashAccount: {
            cashFunds: 25000,
            asOfDate: new ISODate()
        },
        orders: []
    },
    {upsert: true});

db.Portfolios.update({_id: ObjectId("57d630f4777c328f64d38a7b")}, {
        contestID: "57d3472bfee1c54362297f97",
        playerID: "554d8d313400002b00ff4ed5",
        status: "ACTIVE",
        creationTime: new ISODate(),
        cashAccount: {
            cashFunds: 25000,
            asOfDate: new ISODate()
        },
        orders: []
    },
    {upsert: true});

db.Portfolios.update({_id: ObjectId("57c270fd207bc10ca05102dd")}, {
        contestID: "57d3472bfee1c54362297f97",
        playerID: "557fc2895882c8421f3c5580",
        status: "ACTIVE",
        creationTime: new ISODate(),
        cashAccount: {
            cashFunds: 25000,
            asOfDate: new ISODate()
        },
        orders: [{
            _id: ObjectId().valueOf(),
            symbol: "AMD",
            exchange: "NASDAQ",
            accountType: "CASH",
            orderType: "BUY",
            priceType: "MARKET",
            quantity: 1000,
            creationTime: new ISODate("2016-09-16T05:19:47.569Z")
        }, {
            _id: ObjectId().valueOf(),
            symbol: "AAPL",
            exchange: "NASDAQ",
            accountType: "CASH",
            orderType: "BUY",
            priceType: "MARKET",
            quantity: 10,
            creationTime: new ISODate("2016-09-16T05:19:47.569Z")
        }, {
            _id: ObjectId().valueOf(),
            symbol: "INTC",
            exchange: "NASDAQ",
            accountType: "CASH",
            orderType: "BUY",
            priceType: "MARKET",
            quantity: 250,
            creationTime: new ISODate("2016-09-16T05:19:47.569Z")
        }, {
            _id: ObjectId().valueOf(),
            symbol: "MSFT",
            exchange: "NASDAQ",
            accountType: "CASH",
            orderType: "BUY",
            priceType: "MARKET",
            quantity: 250,
            creationTime: new ISODate("2016-09-16T05:19:47.569Z")
        }]
    },
    {upsert: true});