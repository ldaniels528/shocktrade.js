// Facebook SDK injector
(function (fbroot) {
    // is the element our script?
    var id = 'facebook-jssdk';
    if (fbroot.getElementById(id)) {
        return;
    }

    // dynamically create the script
    var js = fbroot.createElement('script');
    js.id = id;
    js.async = true;
    js.src = "http://connect.facebook.net/en_US/all.js";

    // get the script and insert our dynamic script
    var ref = fbroot.getElementsByTagName('script')[0];
    ref.parentNode.insertBefore(js, ref);
}(document));

/**
 * Returns the Facebook application ID based on the running host
 * @returns {*}
 */
window.getAppId = function () {
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
    // initialize the Facebook SDK
    FB.init({
        appId: getAppId(),
        status: true,
        xfbml: true
    });

    // get the login status
    FB.getLoginStatus(function (response) {
        if (response.status === 'connected') {
            // capture the Facebook login status
            if (response.authResponse) {
                console.log("Successfully loaded the Facebook profile...");

                // capture the user ID and access token
                var rootElem = $("#ShockTradeMain");
                var injector = angular.element(rootElem).injector();
                var Facebook = injector.get("Facebook");
                if (Facebook) {
                    Facebook.FB = FB;
                    Facebook.appID = getAppId();
                    Facebook.auth = response.authResponse;
                    Facebook.userID = response.authResponse.userID;
                    Facebook.accessToken = response.authResponse.accessToken;
                }
                else {
                    console.log("Facebook service could not be retrieved");
                }

                // react the the login status
                var scope = angular.element(rootElem).scope();
                if (scope) {
                    scope.$apply(function () {
                        scope.postLoginUpdates(Facebook.userID, false);
                    });
                }
                else {
                    console.log("scope could not be retrieved");
                }
            }
        }
    });

};
