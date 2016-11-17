/**
 * ShockTrade Control Panel Application Bootstrap
 * @author: lawrence.daniels@gmail.com
 */
(function () {
    require("./shocktrade-controlpanel-fastopt.js");
    const facade = com.shocktrade.controlpanel.ControlPanelJsApp();
    facade.startServer({
        "__dirname": __dirname,
        "__filename": __filename,
        "exports": exports,
        "module": module,
        "require": require
    });
})();
