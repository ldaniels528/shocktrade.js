@(maxPlayers: Int)

(function() {

    /**
     * ShockTrade Angular.js Application
     */
    var app = angular.module('shocktrade', ['ngCookies', 'ngRoute', 'ngSanitize', 'ui.bootstrap' ]);

    /**
     * Configure the application
     */
    app.config(['$locationProvider', '$parseProvider', '$routeProvider',
        function($locationProvider, $parseProvider, $routeProvider) {
            // remove the # from URLs
            // $locationProvider.html5Mode(true);

            // now we can directly consume promises
            //$parseProvider.unwrapPromises(true);

            // setup the routes
            $routeProvider.
                when('/blog', { templateUrl: '/assets/views/blog/postings.htm', controller: 'BlogCtrl' }).
                when('/blog/management', { templateUrl: '/assets/views/blog/management.htm', controller: 'BlogCtrl' }).
                when('/connect', { templateUrl: '/assets/views/connect/connect.htm', controller: 'ConnectCtrl' }).
                when('/developers', { templateUrl: '/assets/views/developers/developer.htm', controller: 'DeveloperCtrl' }).
                when('/discover', { templateUrl: '/assets/views/discover/discover.htm', controller: 'DiscoverCtrl' }).
                when('/discover/:symbol', { templateUrl: '/assets/views/discover/discover.htm', controller: 'DiscoverCtrl' }).
                when('/explore', { templateUrl: '/assets/views/explore/drill_down.htm', controller: 'DrillDownCtrl'  }).
                when('/news', { templateUrl: '/assets/views/news/index.htm', controller: 'NewsCtrl'  }).
                when('/play', { redirectTo: '/play/search' }).
                when('/play/awards', { templateUrl: '/assets/views/play/awards/index.htm' }).
                when('/play/lounge', { templateUrl: '/assets/views/play/lounge/index.htm' }).
                when('/play/perks', { templateUrl: '/assets/views/play/perks/index.htm' }).
                when('/play/portfolio', { templateUrl: '/assets/views/play/portfolio/index.htm' }).
                when('/play/lobby', { templateUrl: '/assets/views/play/lobby/index.htm' }).
                when('/play/search', { templateUrl: '/assets/views/play/search/index.htm' }).
                when('/play/statistics', { templateUrl: '/assets/views/play/statistics/index.htm' }).
                when('/search', { templateUrl: '/assets/views/search/index.htm' }).
                otherwise({ redirectTo: '/discover' });
        }]);

    app.run(function($rootScope,
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

    });

})();