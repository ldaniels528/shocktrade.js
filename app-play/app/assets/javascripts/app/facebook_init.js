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
 * Asynchronously load the Facebook SDK
 */
window.fbAsyncInit = function () {
    var attemptsLeft = 5;

    /**
     * Initializes the Facebook SDK
     * @param appId the given application ID
     */
    function fbInit(appId) {
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
    }

    /**
     * Ensures the Facebook SDK is loaded using ShockTrade App ID
     */
    function doInit() {
        if (getShockTradeAppID) {
            fbInit(getShockTradeAppID());
        }
        else if (--attemptsLeft > 0) {
            console.log("Facebook Service not loaded yet... Retry in 500ms (attempts remaining = " + attemptsLeft + ")");
            setTimeout(function () {
                doInit();
            }, 500)
        }
    }

    // initialize the Facebook SDK
    (function () {
        doInit();
    })();

};
