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
                    volumeMin: 1e+5,
                    avgVolumeMin: 1e+6,
                    sortFields: [{field:"avgVolume10Day", direction:-1}, {field:"spread", direction:-1}, {field:"lastTrade", direction:1}]
                },
                rules: [{
                    name: "Owned Securities Exclusions",
                    exclude: [{
                        symbol: {
                            in: ["positions"]
                        }
                    }]
                }, {
                    name: "Pending Securities Exclusions",
                    exclude: [{
                        symbol: {
                            in: ["orders"]
                        }
                    }]
                }, {
                    name: "Poor Standing Exclusions",
                    exclude: [{
                        advisory: "WARNING"
                    }]
                }]
            },
            sellingFlow: {
                profitTarget: 25.0
            }
        }
    }, {upsert: true});

db.Robots.update(
    {playerID: "51a308ac50c70a97d375a6b5"},
    {
        playerID: "51a308ac50c70a97d375a6b5",
        name: "Daisy-Bot",
        lastActivated: new ISODate(),
        active: true,
        tradingStrategy: {
            name: "Girl Power",
            buyingFlow: {
                preferredSpendPerSecurity: 1000.00,
                searchOptions: {
                    changeMin: -0.10,
                    spreadMin: 25.0,
                    priceMin: 0.0001,
                    priceMax: 2.00,
                    volumeMin: 1e+5,
                    avgVolumeMin: 1e+6,
                    sortFields: [{field:"avgVolume10Day", direction:-1}, {field:"spread", direction:-1}, {field:"lastTrade", direction:1}]
                },
                rules: [{
                    name: "Owned Securities Exclusions",
                    exclude: [{
                        symbol: {
                            in: ["positions"]
                        }
                    }]
                }, {
                    name: "Pending Securities Exclusions",
                    exclude: [{
                        symbol: {
                            in: ["orders"]
                        }
                    }]
                }, {
                    name: "Poor Standing Exclusions",
                    exclude: [{
                        advisory: "WARNING"
                    }]
                }]
            },
            sellingFlow: {
                profitTarget: 25.0
            }
        }
    }, {upsert: true});