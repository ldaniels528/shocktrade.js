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
                .when('/awards', {templateUrl: '/assets/views/awards/awards.htm', controller: 'AwardsController'})
                .when('/connect', {templateUrl: '/assets/views/connect/connect.htm', controller: 'ConnectController'})
                .when('/discover', {templateUrl: '/assets/views/discover/discover.htm', controller: 'DiscoverController'})
                .when('/discover/:symbol', {templateUrl: '/assets/views/discover/discover.htm', controller: 'DiscoverController'})
                .when('/explore', {templateUrl: '/assets/views/explore/drill_down.htm', controller: 'DrillDownController'})
                .when('/dashboard', {templateUrl: '/assets/views/play/dashboard.htm', controller: 'DashboardController'})
                .when('/dashboard/:contestId', {templateUrl: '/assets/views/play/dashboard.htm', controller: 'DashboardController'})
                .when('/news', {templateUrl: '/assets/views/news/news_index.htm', controller: 'NewsController'})
                .when('/research', {templateUrl: '/assets/views/research/index.htm'})
                .when('/search', {templateUrl: '/assets/views/play/search.htm'})
                .when('/statistics', {templateUrl: '/assets/views/statistics/statistics.htm'})
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

    });

})();