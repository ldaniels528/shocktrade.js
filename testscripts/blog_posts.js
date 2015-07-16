db.Blog.update({}, {$set: { "author" : "Lawrence Daniels", creationTime : new Date()  }}, { multi:true});

db.Blog.update({title: "System Architecture"}, {$set: { "author" : "Lawrence Daniels", creationTime : new Date() }});

db.Blog.insert({
	"creationTime" : new Date(),
	"title" : "About Me",
	"body" : "My name is Lawrence Daniels (lawrence.daniels@gmail.com) and I am a professional Software Architect, specializing in the architecture, design, and implementation of distributed, high performance and highly scalable systems. The original idea for ShockTrade came to me while studying for my MBA at Texas A&M University. I created an executive summary for a business plan during an Entrepreneurship course. The basic premise: provide consumers with a simple and easy-to-use investment tool, and consumers who currently shy away from investing could become successful investors."
});

db.Blog.insert({
	"creationTime" : new Date(),
	"title" : "What is ShockTrade?",
	"body" : "<span style='color: #0000ff'>ShockTrade</span> is an Online Interactive Trading Simulation and Investment Research System (stocks, ETFs, fixed income securities, and currencies). The system allows consumers to easily identify investment opportunities (that they would not have been able to discover otherwise) via a simple easy-to-use web-based interface. The system can be used to research stocks (and other investment vehicles) in near real-time (during trading hours) or historically via a user-defined date range and investment criteria."
})