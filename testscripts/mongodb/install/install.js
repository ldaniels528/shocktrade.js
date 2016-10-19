db.Securities.createIndex({ "symbol": 1 });
db.Securities.createIndex({ "name": 1 });

db.Snapshots.createIndex({ "tradeDateTime": 1 }, { expireAfterSeconds: 3600*24*10 } );

db.RssFeeds.insert({"name" : "CNN Money: Markets", "url" : "http://rss.cnn.com/rss/money_markets.rss", "priority" : 1 });
db.RssFeeds.insert({"name" : "CNN Money: Latest News", "url" : "http://rss.cnn.com/rss/money_latest.rss", "priority" : 2 });
db.RssFeeds.insert({"name" : "CBNC News", "url" : "http://www.cnbc.com/id/100003114/device/rss/rss", "priority" : 3 });
db.RssFeeds.insert({"name" : "MarketWatch: Real-time Headlines", "url" : "http://feeds.marketwatch.com/marketwatch/realtimeheadlines/", "priority" : 4 });
db.RssFeeds.insert({"name" : "MarketWatch: Stocks to Watch", "url" : "http://feeds.marketwatch.com/marketwatch/StockstoWatch/", "priority" : 5 });
db.RssFeeds.insert({"name" : "NASDAQ Stocks News", "url" : "http://articlefeeds.nasdaq.com/nasdaq/categories?category=Stocks", "priority" : 6 });

db.Awards.insert({"name" : "Told your friends!", "code" : "FACEBOOK", "icon" : "images/accomplishments/facebook.jpg", "description" : "Posted to FaceBook from ShockTrade" });
db.Awards.insert({"name" : "Right back at cha!", "code" : "FBLIKEUS", "icon" : "images/accomplishments/facebook.jpg", "description" : "<i>Told your friends</i> and <span class='facebook'><img src='images/contests/icon_facebook.jpg'>Liked</span> ShockTrade on FaceBook (<i>Pays 1 Perk</i>)" });
db.Awards.insert({"name" : "A Little bird told me...", "code" : "TWITTER", "icon" : "images/accomplishments/twitter.png", "description" : "Posted a Tweet from ShockTrade" });
db.Awards.insert({"name" : "Your colleagues had to know!", "code" : "LINKEDIN", "icon" : "images/accomplishments/linkedin.png", "description" : "Posted to LinkedIn from ShockTrade" });
db.Awards.insert({"name" : "Told your followers!", "code" : "GOOGPLUS", "icon" : "images/accomplishments/google_plus.jpg", "description" : "Posted to Google+ from ShockTrade" });
db.Awards.insert({"name" : "A Picture is worth a thousand words!", "code" : "INSTGRAM", "icon" : "images/accomplishments/instagram.png", "description" : "Posted to Instagram from ShockTrade" });
db.Awards.insert({"name" : "Self-promotion pays!", "code" : "MEPROMO", "icon" : "images/accomplishments/instagram.png", "description" : "Posted to FaceBook, Google+, Instagram, LinkedIn and Twitter from ShockTrade (<i>Pays 1 Perk</i>)" });
db.Awards.insert({"name" : "The Ultimate Socialite!", "code" : "SOCLITE", "icon" : "images/accomplishments/instagram.png", "description" : "Earned all social awards" });
db.Awards.insert({"name" : "Perks of the Job!", "code" : "PERK", "icon" : "images/accomplishments/perk.gif", "description" : "Earned a Perk" });
db.Awards.insert({"name" : "It's time for the Perk-u-lator!", "code" : "5PERKS", "icon" : "images/accomplishments/perk.gif", "description" : "Earned 5 Perks" });
db.Awards.insert({"name" : "Perk Master!", "code" : "10PERKS", "icon" : "images/accomplishments/perk.gif", "description" : "Earned 10 Perks" });
db.Awards.insert({"name" : "Euro-Tactular!", "code" : "EUROTACT", "icon" : "images/accomplishments/euro-tactular.png", "description" : "Traded the Euro" });
db.Awards.insert({"name" : "International Shopper", "code" : "INTNSHPR", "icon" : "images/accomplishments/international_shopper.gif", "description" : "Traded three or more currencies" });
db.Awards.insert({"name" : "Pay Dirt!", "code" : "PAYDIRT", "icon" : "images/accomplishments/pay_dirt.png", "description" : "Your portfolio gained 100% or more" });
db.Awards.insert({"name" : "Mad Money!", "code" : "MADMONEY", "icon" : "images/accomplishments/made_money.png", "description" : "Your portfolio gained 250% or more" });
db.Awards.insert({"name" : "Crystal Ball", "code" : "CRYSTBAL", "icon" : "images/accomplishments/crystal_ball.png", "description" : "Your portfolio gained 500% or more" });
db.Awards.insert({"name" : "Checkered Flag", "code" : "CHKDFLAG", "icon" : "images/accomplishments/checkered_flag.png", "description" : "Finished a Game!" });
db.Awards.insert({"name" : "Gold Trophy", "code" : "GLDTRPHY", "icon" : "images/accomplishments/gold_trophy.png", "description" : "Came in first place! (out of 14 players)" });

db.Perks.insert({"code" : "PRCHEMNT", "name" : "Purchase Eminent", "cost" : 500, "description" : "Gives the player the ability to create SELL orders for securities not yet owned" });
db.Perks.insert({"code" : "PRFCTIMG", "name" : "Perfect Timing", "cost" : 500, "description" : "Gives the player the ability to create BUY orders for more than cash currently available" });
db.Perks.insert({"code" : "CMPDDALY", "name" : "Compounded Daily", "cost" : 1000, "description" : "Gives the player the ability to earn interest on cash not currently invested" });
db.Perks.insert({"code" : "FEEWAIVR", "name" : "Fee Waiver", "cost" : 2500, "description" : "Reduces the commissions the player pays for buying or selling securities" });
db.Perks.insert({"code" : "MARGIN", "name" : "Rational People think at the Margin", "cost" : 2500, "description" : "Gives the player the ability to use margin accounts" });
db.Perks.insert({"code" : "SAVGLOAN", "name" : "Savings and Loans", "cost" : 5000, "description" : "Gives the player the ability to borrow money" });
db.Perks.insert({"code" : "LOANSHRK", "name" : "Loan Shark", "cost" : 5000, "description" : "Gives the player the ability to loan other players money at any interest rate" });
db.Perks.insert({"code" : "MUTFUNDS", "name" : "The Feeling's Mutual", "cost" : 5000, "description" : "Gives the player the ability to create and use mutual funds" });
db.Perks.insert({"code" : "RISKMGMT", "name" : "Risk Management", "cost" : 5000, "description" : "Gives the player the ability to trade options" });