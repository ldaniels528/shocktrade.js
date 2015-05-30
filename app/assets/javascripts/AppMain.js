(function() {

    /**
     * ShockTrade Angular.js Application
     */
    var app = angular.module('shocktrade', ['ngAnimate', 'ngCookies', 'ngRoute', 'ngSanitize', 'nvd3ChartDirectives', 'toaster', 'ui.bootstrap']);

    /**
     * Configure the application
     */
    app.config(['$routeProvider',
        function ($routeProvider) {
            // setup the routes
            $routeProvider
                .when('/awards', {templateUrl: '/assets/views/awards/awards.htm', controller: 'AwardsController'})
                .when('/connect', {templateUrl: '/assets/views/connect/connect.htm', controller: 'ConnectController'})
                .when('/dashboard', {templateUrl: '/assets/views/dashboard/dashboard.htm', controller: 'DashboardController'})
                .when('/dashboard/:contestId', {templateUrl: '/assets/views/dashboard/dashboard.htm', controller: 'DashboardController'})
                .when('/discover', {templateUrl: '/assets/views/discover/discover.htm', controller: 'DiscoverController'})
                .when('/discover/:symbol', {templateUrl: '/assets/views/discover/discover.htm', controller: 'DiscoverController'})
                .when('/explore', {templateUrl: '/assets/views/explore/drill_down.htm', controller: 'DrillDownController'})
                .when('/inspect/:contestId', {templateUrl: '/assets/views/admin/inspect.htm', controller: 'InspectController'})
                .when('/news', {templateUrl: '/assets/views/news/news_center.htm', controller: 'NewsController'})
                .when('/research', {templateUrl: '/assets/views/research/research.htm', controller: 'ResearchController'})
                .when('/search', {templateUrl: '/assets/views/play/search.htm', controller: 'GameSearchController'})
                .when('/profile', {templateUrl: '/assets/views/profile/profile.htm', controller: 'ProfileController'})
                .otherwise({redirectTo: '/discover'});
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