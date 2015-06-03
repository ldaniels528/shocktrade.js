(function () {
    var app = angular.module('shocktrade');

    /**
     * Main Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('MainController', ['$scope', '$http', '$interval', '$location', '$log', '$timeout', 'toaster', 'ContestService', 'Facebook', 'FavoriteSymbols', 'HeldSecurities', 'MarketStatus', 'MySession', 'ProfileService', 'RecentSymbols', 'SignUpDialog',
        function ($scope, $http, $interval, $location, $log, $timeout, toaster, ContestService, Facebook, FavoriteSymbols, HeldSecurities, MarketStatus, MySession, ProfileService, RecentSymbols, SignUpDialog) {
            // setup the loading mechanism
            $scope._loading = false;
            $scope.loading = false;
            $scope.admin = false;

            // mapping of online players
            var onlinePlayers = {};

            ///////////////////////////////////////////////////////////////////////////
            //          Public Functions
            ///////////////////////////////////////////////////////////////////////////

            $scope.mainInit = function(uuid) {
                console.log("Session UUID is " + uuid);
            };

            $scope.abs = function (value) {
                return !value ? value : ((value < 0) ? -value : value);
            };

            $scope.getDate = function(date) {
                return date && date.$date ? date.$date : date;
            };

            $scope.getHtmlQuote = function (q) {
                if(!q) return "";
                else {
                    var html = "<i class='" + $scope.getAssetIcon(q) + "'></i> " + q.symbol + ' - ' + q.name;
                    console.log("html = " + html);
                    return html;
                }
            };

            $scope.getAssetCode = function (q) {
                if (!q) return null;
                else {
                    switch (q.assetType) {
                        case 'Crypto-Currency':
                            return "&#xf15a;"; // fa-bitcoin
                        case 'Currency':
                            return "&#xf155;"; // fa-dollar
                        case 'ETF':
                            return "&#xf18d;"; // fa-stack-exchange
                        default:
                            return "&#xf0ac;"; // fa-globe
                    }
                }
            };

            $scope.getAssetIcon = function (q) {
                if (!q || !q.assetType) return "fa fa-globe st_blue";
                else {
                    switch (q.assetType) {
                        case 'Crypto-Currency':
                            return "fa fa-bitcoin st_blue";
                        case 'Currency':
                            return "fa fa-dollar st_blue";
                        case 'ETF':
                            return "fa fa-stack-exchange st_blue";
                        default:
                            return "fa fa-globe st_blue";
                    }
                }
            };

            /**
             * Returns the contacts matching the given search term
             */
            $scope.getRegisteredFriends = function () {
                return MySession.fbFriends;
            };

            $scope.getBarRanking = function () {
                if (MySession.contestIsEmpty() || !MySession.getUserName()) return null;
                else {
                    var rankings = ContestService.getPlayerRankings(MySession.getContest(), MySession.getUserName());
                    return rankings.player;
                }
            };

            $scope.changePlayTab = function (tabIndex) {
                var tab = $scope.playTabs[tabIndex];
                $log.info("Changing location to " + tab.url);
                $location.path(tab.url);
                return true;
            };

            $scope.getTabIndex = function () {
                return determineTableIndex();
            };

            function determineTableIndex() {
                var path = $location.path();
                if (path.indexOf("/connect") != -1) return 1;
                else if (path.indexOf("/discover") != -1) return 2;
                else if (path.indexOf("/explore") != -1) return 2;
                else if (path.indexOf("/dashboard") != -1) return 1;
                else if (path.indexOf("/news") != -1) return 2;
                else if (path.indexOf("/research") != -1) return 3;
                else if (path.indexOf("/search") != -1) return 0;
                else if (path.indexOf("/profile") != -1) return 4;
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
                        console.log(friends.length + " friends loaded");
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

            $scope.signUpPopup = function (fbUserID, fbProfile) {
                SignUpDialog.popup(fbUserID, fbProfile);
            };

            //////////////////////////////////////////////////////////////////////
            //              Quote-related Functions
            //////////////////////////////////////////////////////////////////////

            $scope.normalizeExchange = function (market) {
                if (market == null) return null;
                else {
                    var s = market.toUpperCase();
                    if (s.indexOf("ASE") == 0) return s;
                    else if (s.indexOf("CCY") == 0) return s;
                    else if (s.indexOf("NAS") == 0) return "NASDAQ";
                    else if (s.indexOf("NCM") == 0) return "NASDAQ";
                    else if (s.indexOf("NGM") == 0) return "NASDAQ";
                    else if (s.indexOf("NMS") == 0) return "NASDAQ";
                    else if (s.indexOf("NYQ") == 0) return "NYSE";
                    else if (s.indexOf("NYS") == 0) return "NYSE";
                    else if (s.indexOf("OBB") == 0) return "OTCBB";
                    else if (s.indexOf("OTC") == 0) return "OTCBB";
                    else if (s.indexOf("OTHER") == 0) return "OTHER_OTC";
                    else if (s.indexOf("PCX") == 0) return s;
                    else if (s.indexOf("PNK") == 0) return "OTCBB";
                    else {
                        $log.warn("exchange = " + s);
                        return s;
                    }
                }
            };

            $scope.getExchangeClass = function (exchange) {
                return $scope.normalizeExchange(exchange) + ' bold';
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

            //////////////////////////////////////////////////////////////////////
            //              Data Graphs
            //////////////////////////////////////////////////////////////////////

            // setup the tabs
            $scope.playTabs = [{
                "name": "Search",
                "icon_class": "fa-search",
                "tool_tip": "Search for games",
                "url": "/search",
                "isVisible": function () {
                    return true;
                }
            }, {
                "name": "Dashboard",
                "icon_class": "fa-gamepad",
                "tool_tip": "Main game dashboard",
                "url": "/dashboard",
                "isVisible": function () {
                    return !MySession.contestIsEmpty();
                }
            }, {
                "name": "Discover",
                "icon_class": "fa-newspaper-o",
                "tool_tip": "Stock News and Quotes",
                "url": "/discover",
                "isVisible": function () {
                    return true;
                }
            }, {
                "name": "Research",
                "icon_class": "fa-table",
                "tool_tip": "Stock Research",
                "url": "/research",
                "isVisible": function () {
                    return true;
                }
            }, {
                "name": "My Profile",
                "icon_class": "fa-user",
                "tool_tip": "My Profile",
                "url": "/profile",
                "isVisible": function () {
                    return MySession.authenticated;
                }
            }];

            // define the levels
            $scope.levels = [
                {"number": 1, "nextLevelXP": 1000, "description": "Private"},
                {"number": 2, "nextLevelXP": 2000, "description": "Private 1st Class"},
                {"number": 3, "nextLevelXP": 4000, "description": "Corporal"},
                {"number": 4, "nextLevelXP": 8000, "description": "First Corporal"},
                {"number": 5, "nextLevelXP": 16000, "description": "Sergeant"},
                {"number": 6, "nextLevelXP": 32000, "description": "Staff Sergeant"},
                {"number": 7, "nextLevelXP": 64000, "description": "Gunnery Sergeant"},
                {"number": 8, "nextLevelXP": 1280000, "description": "Master Sergeant"},
                {"number": 9, "nextLevelXP": 256000, "description": "First Sergeant"},
                {"number": 10, "nextLevelXP": 1024000, "description": "Sergeant Major"},
                {"number": 11, "nextLevelXP": 2048000, "description": "Warrant Officer 3rd Class"},
                {"number": 12, "nextLevelXP": 4096000, "description": "Warrant Officer 2nd Class"},
                {"number": 13, "nextLevelXP": 4096000, "description": "Warrant Officer 1st Class"},
                {"number": 14, "nextLevelXP": 8192000, "description": "Chief Warrant Officer"},
                {"number": 15, "nextLevelXP": 8192000, "description": "Master Chief Warrant Officer"},
                {"number": 16, "nextLevelXP": 16384000, "description": "Lieutenant"},
                {"number": 17, "nextLevelXP": 32768000, "description": "First Lieutenant"},
                {"number": 18, "nextLevelXP": 65536000, "description": "Captain"},
                {"number": 19, "nextLevelXP": 131072000, "description": "Major"},
                {"number": 20, "nextLevelXP": 262144000, "description": "Lieutenant Colonel"},
                {"number": 21, "nextLevelXP": 524288000, "description": "Colonel"},
                {"number": 22, "nextLevelXP": 524288000, "description": "Brigadier General"},
                {"number": 23, "nextLevelXP": 524288000, "description": "Major General"},
                {"number": 24, "nextLevelXP": 524288000, "description": "Lieutenant General"},
                {"number": 25, "nextLevelXP": 524288000, "description": "General"}];

            //////////////////////////////////////////////////////////////////////
            //              Event Listeners
            //////////////////////////////////////////////////////////////////////

            $scope.$on("user_status_changed", function (event, newState) {
                $log.info("user_status_changed: newState = " + angular.toJson(newState));
            });

            // watch for changes to the player's profile
            $scope.$watch("MySession.userProfile", function () {
                if (!MySession.userProfile.favorites) MySession.userProfile.favorites = ['AAPL'];
                if (!MySession.userProfile.recentSymbols) MySession.userProfile.recentSymbols = ['AAPL', 'AMZN', 'GOOG', 'MSFT'];

                // load the favorite and recent quotes
                FavoriteSymbols.setSymbols(MySession.userProfile.favorites);
                RecentSymbols.setSymbols(MySession.userProfile.recentSymbols);

                // load the held securities
                var id = MySession.getUserID();
                if (id) {
                    HeldSecurities.init(id);
                }
            });

        }]);
})();