/**
 * ShockTrade Web App Server Bootstrap
 * @author: lawrence.daniels@gmail.com
 */
(function () {
    require("./shocktrade-nodejs-fastopt.js");
    const facade = com.shocktrade.javascript.ShocktradeWebServerJsApp();
    facade.startServer({
        "__dirname": __dirname,
        "__filename": __filename,
        "exports": exports,
        "module": module,
        "require": require
    });
})();
