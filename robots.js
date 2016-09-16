/**
 * ShockTrade Autonomous Trading Server Bootstrap
 * @author: lawrence.daniels@gmail.com
 */
(function () {
    require("./shocktrade-robots-fastopt.js");
    const facade = com.shocktrade.autonomous.AutonomousTradingJsApp();
    facade.startServer({
        "__dirname": __dirname,
        "__filename": __filename,
        "exports": exports,
        "module": module,
        "require": require
    });
})();
