db.Robots.update(
    {playerID: "554d8d313400002b00ff4ed5"},
    {
        playerID: "554d8d313400002b00ff4ed5",
        name: "Gadgie-Bot",
        lastActivated: new ISODate(),
        active: true
    }, {upsert: true});