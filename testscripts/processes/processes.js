
db.BatchProcesses.update({name: "SecuritiesRefresh"}, {
    name: "SecuritiesRefresh",
    enabled: true
}, {upsert:true});