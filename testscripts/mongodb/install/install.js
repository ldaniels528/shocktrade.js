db.Securities.createIndex({ "symbol": 1 });
db.Securities.createIndex({ "name": 1 });

db.Snapshots.createIndex({ "tradeDateTime": 1 }, { expireAfterSeconds: 3600*24*10 } );