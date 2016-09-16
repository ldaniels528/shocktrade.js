// TTL index for IntraDayQuotes - expire after 48 hours
db.IntraDayQuotes.createIndex({"tradeDateTime": 1}, {expireAfterSeconds: 3600 * 48});

// index for IntraDayQuotes updates
db.IntraDayQuotes.ensureIndex({
    "itemNo": 1,
    "pageNo": 1,
    "symbol": 1,
    "price": 1,
    "tradeDateTime": 1,
    "volume": 1
}, {unique: true});



