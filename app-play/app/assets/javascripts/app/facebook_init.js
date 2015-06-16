/**
 * Facebook SDK injector
 */
(function (fbroot) {
    // is the element our script?
    var id = 'facebook-jssdk';
    if (fbroot.getElementById(id)) {
        return;
    }

    // dynamically create the script
    var js = fbroot.createElement("script");
    js.id = id;
    js.async = true;
    js.src = "http://connect.facebook.net/en_US/all.js";

    // get the script and insert our dynamic script
    var ref = fbroot.getElementsByTagName("script")[0];
    ref.parentNode.insertBefore(js, ref);
}(document));

/**
 * Returns the Facebook application ID based on the running host
 * @returns {*}
 */
window.getShockTradeAppID = function () {
    console.log("Facebook - hostname: " + location.hostname);
    switch (location.hostname) {
        case "localhost":
            return "522523074535098"; // local dev
        case "www.shocktrade.biz":
            return "616941558381179";
        case "shocktrade.biz":
            return "616941558381179";
        case "www.shocktrade.com":
            return "364507947024983";
        case "shocktrade.com":
            return "364507947024983";
        case "www.shocktrade.net":
            return "616569495084446";
        case "shocktrade.net":
            return "616569495084446";
        default:
            console.log("Unrecognized hostname '" + location.hostname + "'");
            return "522523074535098"; // unknown, so local dev
    }
};

/**
 * Asynchronously load the Facebook SDK
 */
window.fbAsyncInit = function () {
    var appId = getShockTradeAppID();
    console.log("Initializing Facebook SDK (App ID " + appId + ")...");
    FB.init({
        appId: appId,
        status: true,
        xfbml: true
    });

    // capture the user ID and access token
    var elemName = "#ShockTradeMain";
    var rootElem = $(elemName);
    var injector = angular.element(rootElem).injector();
    var facebook = injector.get("Facebook");
    if (facebook) {
        facebook.init(FB).then(
            function (response) {
                console.log("Facebook login successful.");
                console.log("FB response: " + angular.toJson(response));

                // react the the login status
                var scope = angular.element(rootElem).scope();
                if (scope) {
                    scope.postLoginUpdates(facebook.facebookID, false);
                }
                else console.log("Scope for " + elemName + " could not be retrieved");
            },
            function (err) {
                console.log("Facebook Service: " + err);
            });
    }
    else console.log("Facebook service could not be retrieved");

};
