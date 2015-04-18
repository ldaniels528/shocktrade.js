db.PlayerUpdates.drop();
db.PlayerUpdates.insert({
	creationTime: new Date(),
	userName: "Spectator",
	eventType: "MAIL",
	title: "Welcome to ShockTrade",
	description: "I'd like to welcome you to ShockTrade. Please sign-in (or sign-up!) to access all of the system's features.",
});
db.PlayerUpdates.insert({
	creationTime: new Date(),
	userName: "ldaniels",
	eventType: "ALERT",
	title: "Stock Alert",
	description: "Market Order for 1,000 shares of AMD @ 3.34 expired",
	dismissed: false
});
db.PlayerUpdates.insert({
	creationTime: new Date(),
	userName: "ldaniels",
	sender: { name:"dizorganizer", facebookID:"100003027501772" },
	eventType: "MAIL",
	title: "Been a while ...",
	description: "What's up?",
	dismissed: false
});
db.PlayerUpdates.insert({
	creationTime: new Date(),
	userName: "ldaniels",
	sender: { name:"seralovett", facebookID:"1589191728" },
	eventType: "CHAT",
	title: "Chat Message",
	description: "Coming upstairs?",
	dismissed: false
});
db.PlayerUpdates.insert({
	creationTime: new Date(),
	userName: "ldaniels",
	sender: { name:"natech", facebookID:"1377815655" },
	eventType: "CHAT",
	title: "Chat Message",
	description: "You're on fire!",
	dismissed: false
});
db.PlayerUpdates.insert({
	creationTime: new Date(),
	userName: "ldaniels",
	sender: { name:"tricky" },
	eventType: "CHAT",
	title: "Chat Message",
	description: "Prepare to get whooped up!",
	dismissed: false
});
db.PlayerUpdates.insert({
	creationTime: new Date(),
	userName: "ldaniels",
	sender: { name:"naughtymonkey" },
	eventType: "INVITE",
	title: "Game Invite",
	description: "Come join us",
	dismissed: false
});
db.PlayerUpdates.insert({
	creationTime: new Date(),
	userName: "ldaniels",
	eventType: "ALERT",
	title: "Stock Alert",
	description: "Market Order for 1000 shares of AMD @ 5.34 expired",
	dismissed: false
});