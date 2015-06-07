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

