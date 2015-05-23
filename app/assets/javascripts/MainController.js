(function () {
    var app = angular.module('shocktrade');

    /**
     * Main Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('MainController', ['$scope', '$http', '$interval', '$location', '$log', '$timeout', 'toaster', 'Facebook', 'FavoriteSymbols', 'HeldSecurities', 'InvitePlayerDialog', 'MarketStatus', 'MySession', 'NewGameDialog', 'ProfileService', 'SignUpDialog',
        function ($scope, $http, $interval, $location, $log, $timeout, toaster, Facebook, FavoriteSymbols, HeldSecurities, InvitePlayerDialog, MarketStatus, MySession, NewGameDialog, ProfileService, SignUpDialog) {
            // setup the loading mechanism
            $scope._loading = false;
            $scope.loading = false;

            // setup main-specific variables
            $scope.admin = false;

            // mapping of online players
            var onlinePlayers = {};

            // setup the tabs
            $scope.tabIndex = determineTableIndex();
            $scope.playTabs = [{
                "name": "Search",
                "icon_class": "fa-search",
                "tool_tip": "Search for games",
                "url": "/search",
                "isVisible": function() {
                    return true;
                }
            }, {
                "name": "Play",
                "icon_class": "fa-gamepad",
                "tool_tip": "Main game dashboard",
                "url": "/dashboard",
                "isVisible": function() {
                    return !MySession.contestIsEmpty();
                }
            }, {
                "name": "Discover",
                "icon_class": "fa-newspaper-o",
                "tool_tip": "Stock News and Quotes",
                "url": "/discover",
                "isVisible": function() {
                    return true;
                }
            },{
                "name": "Research",
                "icon_class": "fa-table",
                "tool_tip": "Stock Research",
                "url": "/research",
                "isVisible": function() {
                    return true;
                }
            }, {
                "name": "My Awards",
                "icon_class": "fa-trophy",
                "tool_tip": "My Awards",
                "url": "/awards",
                "isVisible": function() {
                    return true;
                }
            }, {
                "name": "My Statistics",
                "icon_class": "fa-bar-chart",
                "tool_tip": "My Statistics",
                "url": "/statistics",
                "isVisible": function() {
                    return true;
                }
            }];

            $scope.changePlayTab = function (tabIndex) {
                var tab = $scope.playTabs[tabIndex];
                $log.info("Changing location to " + tab.url);
                $location.path(tab.url);
                $scope.tabIndex = tabIndex;
                return true;
            };

            $scope.abs = function (value) {
                return !value ? value : ((value < 0) ? -value : value);
            };

            $scope.clone = function(obj) {
                // Handle the 3 simple types, and null or undefined
                if (null == obj || "object" != typeof obj) return obj;

                // Handle Date
                else if (obj instanceof Date) {
                    var copy = new Date();
                    copy.setTime(obj.getTime());
                    return copy;
                }

                // Handle Array
                else if (obj instanceof Array) {
                    var copy = [];
                    for (var i = 0, len = obj.length; i < len; i++) {
                        copy[i] = $scope.clone(obj[i]);
                    }
                    return copy;
                }

                // Handle Object
                else if (obj instanceof Object) {
                    var copy = {};
                    for (var attr in obj) {
                        if (obj.hasOwnProperty(attr)) copy[attr] = $scope.clone(obj[attr]);
                    }
                    return copy;
                }

                else {
                    throw new Error("Unable to copy object! Its type isn't supported.");
                }
            };

            function determineTableIndex() {
                var path = $location.path();
                if(path.indexOf("/awards") != -1) return 4;
                else if(path.indexOf("/connect") != -1) return 1;
                else if(path.indexOf("/discover") != -1) return 2;
                else if(path.indexOf("/explore") != -1) return 2;
                else if(path.indexOf("/dashboard") != -1) return 1;
                else if(path.indexOf("/news") != -1) return 2;
                else if(path.indexOf("/research") != -1) return 3;
                else if(path.indexOf("/search") != -1) return 0;
                else if(path.indexOf("/statistics") != -1) return 5;
                else return 0;
            }

            $scope.isLoading = function () {
                return $scope._loading;
            };

            $scope.startLoading = function (timeout) {
                $scope._loading = true;
                /*
                 var _timeout = timeout || 4000;

                 // set loading timeout
                 var promise = $timeout(function() {
                 console.log("Disabling the loading animation due to time-out (" + _timeout + " msec)...");
                 $scope.loading = false;
                 }, _timeout);*/
            };

            $scope.stopLoading = function () {
                $timeout(function () {
                    $scope._loading = false;
                }, 500);
            };

            $scope.isOnline = function (player) {
                var playerID = player.facebookID;
                var state = onlinePlayers[playerID];
                if (!state) {
                    state = {connected: false};
                    onlinePlayers[playerID] = state;
                    $http.get("/api/online/" + playerID)
                        .success(function (newState) {
                            onlinePlayers[playerID] = newState;
                        })
                        .error(function (err) {
                            $log.error("Error retrieving online state for user " + playerID);
                        });

                }
                return state && state.connected;
            };

            $scope.range = function (n) {
                return new Array(n);
            };

            $scope.facebookLoginStatus = function (fbUserID) {
                $scope.postLoginUpdates(fbUserID, false);
            };

            $scope.login = function (event) {
                if (event) {
                    event.preventDefault();
                }
                Facebook.login().then(
                    function (response) {
                        var fbUserID = response.authResponse.userID;
                        $scope.postLoginUpdates(fbUserID, true);
                    },
                    function (err) {
                        $log.error("main:login err = " + angular.toJson(err));
                    });
            };

            $scope.logout = function (event) {
                if (event) {
                    event.preventDefault();
                }
                Facebook.logout();
                MySession.logout();
            };

            $scope.postLoginUpdates = function (fbUserID, userInitiated) {
                // capture the user ID
                MySession.fbUserID = fbUserID;

                // load the user's Facebook profile
                Facebook.getUserProfile().then(
                    function (response) {
                        MySession.fbProfile = response;
                        MySession.fbAuthenticated = true;
                    },
                    function (err) {
                        toaster.pop('error', 'Error!', "Facebook login error - " + err.data);
                    });

                // load the user's ShockTrade profile
                ProfileService.getProfileByFacebookID(fbUserID).then(
                    function (profile) {
                        if (!profile.error) {
                            $log.info("ShockTrade user profile loaded...");
                            MySession.userProfile = profile;
                            MySession.authenticated = true;

                            loadFacebookFriends();
                            $scope.filters = MySession.userProfile.filters;
                        }
                        else {
                            $log.info("Non-member identified... Launching Sign-up dialog...");
                            MySession.nonMember = true;
                            $scope.signUpPopup(fbUserID, MySession.fbProfile);
                        }
                    },
                    function (err) {
                        toaster.pop('error', 'Error!', "ShockTrade Profile retrieval error - " + err.data);
                        $scope.signUpPopup(fbUserID, MySession.fbProfile);
                    });
            };

            function loadFacebookFriends() {
                Facebook.getTaggableFriends().then(
                    function (response) {
                        var friends = response.data;
                        MySession.fbFriends = friends.sort(function (a, b) {
                            if (a.name < b.name) return -1;
                            else if (a.name > b.name) return 1;
                            else return 0;
                        });
                    },
                    function (err) {
                        toaster.pop('error', 'Error!', "Failed to retrieve Facebook friends");
                    });
            }

            $scope.newGamePopup = function () {
                NewGameDialog.popup({});
            };

            $scope.invitePlayerPopup = function (participant) {
                InvitePlayerDialog.popup($scope, participant);
            };

            $scope.signUpPopup = function (fbUserID, fbProfile) {
                SignUpDialog.popup(fbUserID, fbProfile);
            };

            $scope.getExchangeClass = function (exchange) {
                if (exchange == null) return null;
                else {
                    var name = exchange.toUpperCase();
                    if (name.indexOf("NASD") != -1) return "NASDAQ";
                    if (name.indexOf("OTC") != -1) return "OTCBB";
                    else return name;
                }
            };

            $scope.getPreferenceIcon = function (q) {
                // fail-safe
                if (!q || !q.symbol) return "";

                // check for favorite and held securities
                var symbol = q.symbol;
                if (HeldSecurities.isHeld(symbol)) return "fa fa-star";
                else if (FavoriteSymbols.isFavorite(symbol)) return "fa fa-heart";
                else return "";
            };

            $scope.$on("user_status_changed", function (event, newState) {
                $log.info("user_status_changed: newState = " + angular.toJson(newState));
            });

        }]);
})();