
db.IntraDayQuotes.find({symbol: "AAPL"}).sort({pageNo:1, itemNo:1, tradeDateTime:1});

db.IntraDayQuotes.find({symbol: "AAPL", time: { $gt : "16:00:00" }}).sort({tradeDateTime:-1});


// http://www.google.com/finance/historical?q=NASDAQ:AAPL&ei=fUrSV8DPJIOtmAGymYTQAw&output=csv