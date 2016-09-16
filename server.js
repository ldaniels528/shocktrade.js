/**
 * ShockTrade Web Application Server Bootstrap
 * @author: lawrence.daniels@gmail.com
 */
(function () {
    require("./shocktrade-webapp-fastopt.js");
    const facade = com.shocktrade.webapp.WebServerJsApp();
    facade.startServer({
        "__dirname": __dirname,
        "__filename": __filename,
        "exports": exports,
        "module": module,
        "require": require
    });
})();
