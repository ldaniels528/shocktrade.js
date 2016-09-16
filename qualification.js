/**
 * ShockTrade Qualification Process Bootstrap
 * @author: lawrence.daniels@gmail.com
 */
(function () {
    require("./app/server/qualification/target/scala-2.11/shocktrade-qualification-fastopt.js");
    const facade = com.shocktrade.qualification.QualificationJsApp();
    facade.startServer({
        "__dirname": __dirname,
        "__filename": __filename,
        "exports": exports,
        "module": module,
        "require": require
    });
})();
