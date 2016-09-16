/**
 * ShockTrade DayCycle Process Bootstrap
 * @author: lawrence.daniels@gmail.com
 */
(function () {
    require("./shocktrade-daycycle-fastopt.js");
    const facade = com.shocktrade.daycycle.DayCycleJsApp();
    facade.startServer({
        "__dirname": __dirname,
        "__filename": __filename,
        "exports": exports,
        "module": module,
        "require": require
    });
})();
