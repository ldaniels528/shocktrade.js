(function () {
    var app = angular.module('shocktrade');

    /**
     * Main Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('MainController', ['$scope', '$interval', '$location', '$log', '$timeout', 'toaster', 'Facebook', 'FavoriteSymbols', 'HeldSecurities', 'MarketStatus', 'MySession', 'ProfileService', 'SignUpDialog',
        function ($scope, $interval, $location, $log, $timeout, toaster, Facebook, FavoriteSymbols, HeldSecurities, MarketStatus, MySession, ProfileService, SignUpDialog) {
            // setup the loading mechanism
            $scope._loading = false;

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

            // setup the market clock
            $scope.marketClock = (new Date()).toTimeString();

            // setup main-specific variables
            $scope.admin = false;

            /**
             * Initializes the application
             */
            $scope.appInit = function () {
                // switch to the Play view
                $location.path("/play");

                // setup market status w/updates
                $interval(function () {
                    $scope.marketClock = (new Date()).toTimeString();
                }, 1000);

                // setup the market status updates
                setupMarketStatusUpdates();
            };

            $scope.alertMessage = function (message) {
                $("#alert_placeholder").html(
                    '<div class="alert">' +
                    '<a class="close" data-dismiss="alert">x</a>' +
                    '<span>' + message + '</span>' +
                    '</div>');
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
                    }
                );
            };

            function loadFacebookFriends() {
                Facebook.getTaggableFriends().then(
                    function (response) {
                        // $log.info("FaceBook friends = " + angular.toJson(response.data, true));
                        MySession.fbFriends = response.data.sort(function (a, b) {
                            if (a.name < b.name) return -1;
                            else if (a.name > b.name) return 1;
                            else return 0;
                        });
                    },
                    function (err) {
                        toaster.pop('error', 'Error!', "Failed to retrieve Facebook friends - " + err.data);
                    });
            }

            $scope.signUpPopup = function(fbUserID, fbProfile) {
                SignUpDialog.popup(fbUserID, fbProfile);
            };

            $scope.abs = function (value) {
                return !value ? value : ((value < 0) ? -value : value);
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
                if (!q || !q.symbol) return "transparent12.png";

                // check for favorite and held securities
                var symbol = q.symbol;
                if (HeldSecurities.isHeld(symbol)) return "star.png";
                else if (FavoriteSymbols.isFavorite(symbol)) return "favorite_small.png";
                else return "transparent12.png";
            };

            function setupMarketStatusUpdates() {
                $scope.usMarketsOpen = null;
                $log.info("Retrieving market status...");
                MarketStatus.getMarketStatus(function (response) {
                    // retrieve the delay in milliseconds from the server
                    var delay = response.delay;
                    if (delay < 0) {
                        delay = response.end - response.sysTime;
                        if (delay <= 300000) {
                            delay = 300000; // 5 minutes
                        }
                    }

                    // set the market status
                    $log.info("US Markets are " + (response.active ? 'Open' : 'Closed') + "; Waiting for " + delay + " msec until next trading start...");
                    setTimeout(function () {
                        $scope.usMarketsOpen = response.active;
                    }, 750);

                    // wait for the delay, then call recursively
                    setTimeout(function () {
                        setupMarketStatusUpdates();
                    }, delay);
                });
            }

        }]);
})();