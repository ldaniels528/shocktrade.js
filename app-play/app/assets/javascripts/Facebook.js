(function () {
    var app = angular.module('shocktrade');

    /**
     * Facebook Service
     * @author lawrence.daniels@gmail.com
     */
    app.factory('Facebook', function ($log, $q) {
        var service = {
            "version": "v2.3",
            "userID": null,
            "auth": null,
            "accessToken": null
        };

        service.getVersion = function() {
            return service.version;
        };

        service.createFriendList = function (friendListId) {
            var deferred = $q.defer();
            FB.api("/v2.3/me/" + friendListId + "/members&access_token=" + service.accessToken,
                function (response) {
                    if (response && !response.error) {
                        deferred.resolve(response);
                    }
                    else {
                        deferred.reject(response);
                    }
                });
            return deferred.promise;
        };

        service.getFriends = function () {
            var deferred = $q.defer();
            FB.api("/v2.3/me/friends?access_token=" + service.accessToken,
                function (response) {
                    if (response && !response.error) {
                        deferred.resolve(response);
                    }
                    else {
                        deferred.reject(response);
                    }
                });
            return deferred.promise;
        };

        service.getTaggableFriends = function () {
            var deferred = $q.defer();
            FB.api("/v2.3/me/taggable_friends?access_token=" + service.accessToken,
                function (response) {
                    if (response && !response.error) {
                        deferred.resolve(response);
                    }
                    else {
                        deferred.reject(response);
                    }
                });
            return deferred.promise;
        };

        service.getFriendList = function (listType) {
            var deferred = $q.defer();
            FB.api("/v2.3/me/friendlists?list_type=" + (listType || "close_friends") + "&access_token=" + service.accessToken,
                function (response) {
                    if (response && !response.error) {
                        deferred.resolve(response);
                    }
                    else {
                        deferred.reject(response);
                    }
                });
            return deferred.promise;
        };

        service.getFriendListMembers = function (friendListId) {
            var deferred = $q.defer();
            FB.api("/v2.3/me/" + friendListId + "/members&access_token=" + service.accessToken,
                function (response) {
                    if (response && !response.error) {
                        deferred.resolve(response);
                    }
                    else {
                        deferred.reject(response);
                    }
                });
            return deferred.promise;
        };

        service.getLoginStatus = function () {
            var deferred = $q.defer();
            FB.getLoginStatus(function (response) {
                if (response.status === 'connected') {
                    // the user is logged in and has authenticated your app, and response.authResponse supplies
                    // the user's ID, a valid access token, a signed request, and the time the access token
                    // and signed request each expire
                    service.userID = response.authResponse.userID;
                    service.accessToken = response.authResponse.accessToken;
                    service.auth = response.authResponse;
                    deferred.resolve(response);
                } else if (response.status === 'not_authorized') {
                    // the user is logged in to Facebook, but has not authenticated your app
                    deferred.reject(response);
                } else {
                    // the user isn't logged in to Facebook.
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };

        service.getUserProfile = function () {
            var deferred = $q.defer();
            FB.api("/v2.3/me?access_token=" + service.auth.accessToken,
                function (response) {
                    if (response && !response.error) {
                        deferred.resolve(response);
                    }
                    else {
                        deferred.reject(response);
                    }
                });
            return deferred.promise;
        };

        service.login = function () {
            var deferred = $q.defer();
            FB.login(function (response) {
                if (response.authResponse) {
                    service.auth = response.authResponse;
                    service.userID = response.authResponse.userID;
                    service.accessToken = response.authResponse.accessToken;
                    deferred.resolve(response);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };

        service.logout = function () {
            var deferred = $q.defer();
            FB.logout(function (response) {
                if (response) {
                    service.auth = null;
                    deferred.resolve(response);
                } else {
                    deferred.reject(response);
                }
            });
            return deferred.promise;
        };

        service.feed = function(caption, link) {
            FB.ui({
                "app_id": getAppId(),
                "method": 'feed',
                "link": link,
                "caption": caption
            }, function(response){});
        };

        service.send = function (message, link) {
            FB.ui({
                "app_id": getAppId(),
                "method": 'send',
                "link": link
            });
        };

        service.share = function (link) {
            FB.ui({
                "app_id": getAppId(),
                "method": 'share',
                "href": link
            }, function (response) {});
        };

        return service;
    });
})();

// Facebook SDK injector
(function (d) {
    // is the element our script?
    var id = 'facebook-jssdk';
    if (d.getElementById(id)) {
        return;
    }

    // dynamically create the script
    var js = d.createElement('script');
    js.id = id;
    js.async = true;
    js.src = "http://connect.facebook.net/en_US/all.js";

    // get the script and insert our dynamic script
    var ref = d.getElementsByTagName('script')[0];
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
                        scope.facebookLoginStatus(Facebook.userID);
                    });
                }
                else {
                    console.log("scope could not be retrieved");
                }
            }
        }
    });

};

