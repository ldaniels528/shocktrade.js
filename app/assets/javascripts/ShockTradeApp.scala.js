@(maxPlayers: Int)
(function() {

    /**
     * ShockTrade Angular.js Application
     */
    var app = angular.module('shocktrade', ['ngAnimate', 'ngCookies', 'ngRoute', 'ngSanitize', 'toaster', 'ui.bootstrap']);

    /**
     * Configure the application
     */
    app.config(['$routeProvider',
        function ($routeProvider) {
            // setup the routes
            $routeProvider
                .when('/awards', {templateUrl: '/assets/views/awards/awards.htm'})
                .when('/connect', {templateUrl: '/assets/views/connect/connect.htm', controller: 'ConnectController'})
                .when('/discover', {templateUrl: '/assets/views/discover/discover.htm', controller: 'DiscoverController'})
                .when('/discover/:symbol', {templateUrl: '/assets/views/discover/discover.htm', controller: 'DiscoverController'})
                .when('/explore', {templateUrl: '/assets/views/explore/drill_down.htm', controller: 'DrillDownController'})
                .when('/portfolio', {templateUrl: '/assets/views/play/lobby/lobby.htm'})
                .when('/lounge', {templateUrl: '/assets/views/play/lounge/lounge.htm'})
                .when('/news', {templateUrl: '/assets/views/news/news_index.htm', controller: 'NewsController'})
                .when('/perks', {templateUrl: '/assets/views/perks/perks.htm'})
                .when('/research', {templateUrl: '/assets/views/research/index.htm'})
                .when('/search', {templateUrl: '/assets/views/play/search/search.htm'})
                .when('/statistics', {templateUrl: '/assets/views/statistics/statistics.htm'})
                .when('/test', {templateUrl: '/assets/views/test.htm'})
                .otherwise({redirectTo: '/search'});
        }]);

    /**
     * Initialize the application
     */
    app.run(function ($rootScope,
                      $location,
                      Facebook,
                      FavoriteSymbols,
                      HeldSecurities,
                      MarketStatus,
                      MySession,
                      NewsSymbols,
                      QuoteService,
                      RecentSymbols,
                      WebSockets) {
        $rootScope.Facebook = Facebook;
        $rootScope.FavoriteSymbols = FavoriteSymbols;
        $rootScope.HeldSecurities = HeldSecurities;
        $rootScope.MarketStatus = MarketStatus;
        $rootScope.MySession = MySession;
        $rootScope.NewsSymbols = NewsSymbols;
        $rootScope.QuoteService = QuoteService;
        $rootScope.RecentSymbols = RecentSymbols;
        $rootScope.WebSockets = WebSockets;
        $rootScope.maxPlayers = parseInt('@maxPlayers');
        $rootScope.loading = false;

        // setup the tabs
        $rootScope.tabIndex = determineTableIndex();
        $rootScope.playTabs = [{
            "name": "Search",
            "icon_class": "fa-search",
            "tool_tip": "Search for games",
            "url": "/search",
            "isVisible": function(c) {
                return true;
            }
        }, {
            "name": "Play",
            "icon_class": "fa-gamepad",
            "tool_tip": "Play the game",
            "url": "/portfolio",
            "isVisible": function(c) {
                return c != null;
            }
        }, {
            "name": "Discover",
            "icon_class": "fa-newspaper-o",
            "tool_tip": "Stock Quotes, News and Research",
            "url": "/discover",
            "isVisible": function(c) {
                return true;
            }
        }, {
            "name": "My Awards",
            "icon_class": "fa-trophy",
            "tool_tip": "My Awards",
            "url": "/awards",
            "isVisible": function(c) { //
                return true;
            }
        }, {
            "name": "My Perks",
            "icon_class": "fa-gift",
            "tool_tip": "My Perks",
            "url": "/perks",
            "isVisible": function(c) {
                return true;
            }
        }, {
            "name": "My Statistics",
            "icon_class": "fa-bar-chart",
            "tool_tip": "My Statistics",
            "url": "/statistics",
            "isVisible": function(c) {
                return true;
            }
        }];

        $rootScope.abs = function (value) {
            return !value ? value : ((value < 0) ? -value : value);
        };

        $rootScope.clone = function(obj) {
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
                    copy[i] = $rootScope.clone(obj[i]);
                }
                return copy;
            }

            // Handle Object
            else if (obj instanceof Object) {
                var copy = {};
                for (var attr in obj) {
                    if (obj.hasOwnProperty(attr)) copy[attr] = $rootScope.clone(obj[attr]);
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
            else if(path.indexOf("/connect") != -1) return 0;
            else if(path.indexOf("/discover") != -1) return 2;
            else if(path.indexOf("/explore") != -1) return 2;
            else if(path.indexOf("/portfolio") != -1) return 1;
            else if(path.indexOf("/lounge") != -1) return 1;
            else if(path.indexOf("/news") != -1) return 3;
            else if(path.indexOf("/perks") != -1) return 5;
            else if(path.indexOf("/research") != -1) return 2;
            else if(path.indexOf("/search") != -1) return 0;
            else if(path.indexOf("/statistics") != -1) return 0;
            else return 0;
        }

    });

    ////////////////////////////////////////////////////////////////////////////
    //      Utility Functions
    ////////////////////////////////////////////////////////////////////////////

    Object.prototype.OID = function() {
        var self = this;
        return self._id ? self._id.$oid : null;
    };

    ////////////////////////////////////////////////////////////////////////////
    //      Filters
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Escape filter
     */
    app.filter('escape', function () {
        return window.escape;
    });

    /**
     * Big Number Filter
     */
    app.filter('bigNumber', function () {
        return function (value) {
            if (value == null || value == "") return "";
            var temp = value.replace(/\,/g, '');
            var num = parseFloat(temp);

            // negative #'s
            if (num <= -1.0e+12)
                return (num / 1.0e+12).toFixed(2) + "T";
            else if (num <= -1.0e+9)
                return (num / 1.0e+9).toFixed(2) + "B";
            else if (num <= -1.0e+6)
                return (num / 1.0e+6).toFixed(2) + "M";
            else if (num <= -1.0e+3)
                return (num / 1.0e+3).toFixed(2) + "K";

            // positive #'s
            else if (num >= 1.0e+12)
                return (num / 1.0e+12).toFixed(2) + "T";
            else if (num >= 1.0e+9)
                return (num / 1.0e+9).toFixed(2) + "B";
            else if (num >= 1.0e+6)
                return (num / 1.0e+6).toFixed(2) + "M";
            else if (num <= 1.0e+3)
                return (num / 1.0e+3).toFixed(2) + "K";

            // default
            else return value;
        };
    });

    /**
     * Quote Change Filter
     */
    app.filter('quoteChange', function () {
        function abs(value) {
            return !value ? value : (value < 0 ? -value : value);
        }

        return function (value) {
            if (value == null) return "";
            var num = abs(parseFloat(value));
            if (num >= 100) return num.toFixed(0);
            else if (num >= 10) return num.toFixed(1);
            else return num.toFixed(2);
        };
    });

    /**
     * Quote Number Filter
     */
    app.filter('quoteNumber', function () {
        return function (value) {
            if (value == null) return "";
            var num = parseFloat(value);
            if (num <= -10000) return num.toFixed(0);
            else if (num <= -100) return num.toFixed(2);
            else if (num <= -10) return num.toFixed(2);
            else if (num <= 0) return num.toFixed(4);
            else if (num < 0.0001) return num.toFixed(5);
            else if (num >= 10000) return num.toFixed(2);
            else if (num >= 100) return num.toFixed(2);
            else if (num >= 10) return num.toFixed(3);
            else if (num >= 1) return num.toFixed(4);
            else return num.toFixed(4);
        };
    });

})();