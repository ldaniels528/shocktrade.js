@(maxPlayers: Int)
(function() {

    /**
     * ShockTrade Angular.js Application
     */
    var app = angular.module('shocktrade', ['ngCookies', 'ngRoute', 'ngSanitize', 'ui.bootstrap']);

    /**
     * Configure the application
     */
    app.config(['$locationProvider', '$parseProvider', '$routeProvider',
        function ($locationProvider, $parseProvider, $routeProvider) {
            // remove the # from URLs
            //$locationProvider.html5Mode(true);

            // now we can directly consume promises
            //$parseProvider.unwrapPromises(true);

            // setup the routes
            $routeProvider
                .when('/connect', {templateUrl: '/assets/views/connect/connect.htm', controller: 'ConnectController'})
                .when('/developers', {templateUrl: '/assets/views/developers/developer.htm', controller: 'DeveloperCtrl'})
                .when('/discover', {templateUrl: '/assets/views/discover/discover.htm', controller: 'DiscoverController'})
                .when('/discover/:symbol', {templateUrl: '/assets/views/discover/discover.htm', controller: 'DiscoverController'})
                .when('/explore', {templateUrl: '/assets/views/explore/drill_down.htm', controller: 'DrillDownController'})
                .when('/news', {templateUrl: '/assets/views/news/index.htm', controller: 'NewsController'})
                .when('/play', {redirectTo: '/play/search'})
                .when('/play/awards', {templateUrl: '/assets/views/play/awards/index.htm'})
                .when('/play/lounge', {templateUrl: '/assets/views/play/lounge/index.htm'})
                .when('/play/perks', {templateUrl: '/assets/views/play/perks/index.htm'})
                .when('/play/portfolio', {templateUrl: '/assets/views/play/portfolio/index.htm'})
                .when('/play/lobby', {templateUrl: '/assets/views/play/lobby/index.htm'})
                .when('/play/search', {templateUrl: '/assets/views/play/search/index.htm'})
                .when('/play/statistics', {templateUrl: '/assets/views/play/statistics/index.htm'})
                .when('/search', {templateUrl: '/assets/views/search/index.htm'})
                .otherwise({redirectTo: '/discover'});
        }]);

    /**
     * Initialize the application
     */
    app.run(function ($rootScope,
                      Errors,
                      Facebook,
                      FavoriteSymbols,
                      HeldSecurities,
                      MarketStatus,
                      MySession,
                      NewsSymbols,
                      QuoteService,
                      RecentSymbols,
                      WebSockets) {
        $rootScope.Errors = Errors;
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