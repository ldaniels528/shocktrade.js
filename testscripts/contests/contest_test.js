db.Contests.update({name: "The World is Mine too"}, {
        name: "The World is Mine too",
        status: "ACTIVE",
        creator: {
            _id: "557fc2895882c8421f3c5580",
            name: "Fugitive528",
            facebookID: "1796471892"
        },
        creationTime: new ISODate(),
        startTime: new ISODate(),
        startingBalance: 25000,
        expirationTime: ISODate("2017-08-02T17:48:36.350+0000"),
        participants: [{
            _id: "557fc2895882c8421f3c5580",
            name: "Fugitive528",
            facebookID: "1796471892",
            joinedTime: new ISODate()
        }, {
            _id: "554d8d313400002b00ff4ed5",
            name: "Gadget",
            facebookID: "613646325380649",
            joinedTime: new ISODate()
        }, {
            _id: "51a308ac50c70a97d375a6b5",
            name: "Daisy",
            facebookID: "100001992439064",
            joinedTime: new ISODate()
        }]
    },
    {upsert: true});
