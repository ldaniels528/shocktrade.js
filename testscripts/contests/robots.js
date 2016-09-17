db.Robots.update(
    {playerID: "554d8d313400002b00ff4ed5"},
    {
        playerID: "554d8d313400002b00ff4ed5",
        name: "Gadgie-Bot",
        lastActivated: new ISODate(),
        active: true
    }, {upsert: true});

db.Robots.update(
    {playerID: "554d8d313400002b00ff4ed5"},
    {
        playerID: "554d8d313400002b00ff4ed5",
        name: "Gadgie-Bot",
        lastActivated: new ISODate(),
        active: true,
        tradingStrategy: {
            name: "Secret Sauce",
            buyingFlow: {
                preferredSpendPerSecurity: 1000.00,
                searchOptions: {
                    changeMin: -0.10,
                    spreadMin: 25.0,
                    priceMin: 0.0001,
                    priceMax: 2.00,
                    avgVolumeMin: 1e+6
                },
                rules: [{
                    name: "Owned Securities",
                    exclude: [{
                        symbol: {
                            in: ["positions", "orders"]
                        },
                        advisory: "WARNING"
                    }],
                    sortBy: {"avgVolume10Day": -1, "spread": -1, "lastTrade": 1}
                }]
            },
            sellingFlow: {}
        }
    }, {upsert: true});