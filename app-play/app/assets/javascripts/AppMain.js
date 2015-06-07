(function() {

    /**
     * ShockTrade Angular.js Application
     */
    var app = angular.module("shocktrade", ["ngAnimate", "ngCookies", "ngRoute", "ngSanitize", "nvd3ChartDirectives", "toaster", "ui.bootstrap"]);

    /**
     * Configure the application
     */
    app.config(["$routeProvider",
        function ($routeProvider) {
            // setup the routes
            $routeProvider
                .when("/connect", {templateUrl: "/assets/views/connect/connect.htm", controller: "ConnectController"})
                .when("/dashboard", {templateUrl: "/assets/views/dashboard/dashboard.htm", controller: "DashboardController"})
                .when("/dashboard/:contestId", {templateUrl: "/assets/views/dashboard/dashboard.htm", controller: "DashboardController"})
                .when("/discover", {templateUrl: "/assets/views/discover/discover.htm", controller: "DiscoverController"})
                .when("/discover/:symbol", {templateUrl: "/assets/views/discover/discover.htm", controller: "DiscoverController"})
                .when("/explore", {templateUrl: "/assets/views/explore/drill_down.htm", controller: "DrillDownController"})
                .when("/inspect/:contestId", {templateUrl: "/assets/views/admin/inspect.htm", controller: "InspectController"})
                .when("/news", {templateUrl: "/assets/views/news/news_center.htm", controller: "NewsController"})
                .when("/research", {templateUrl: "/assets/views/research/research.htm", controller: "ResearchController"})
                .when("/search", {templateUrl: "/assets/views/play/search.htm", controller: "GameSearchController"})
                .when("/symbols/favorites?:symbol", {templateUrl: "/assets/views/discover/favorites.htm", reloadOnSearch: false, controller: "FavoritesController"})
                .when("/symbols", {redirectTo: "/symbols/favorites"})
                .when("/profile/awards", {templateUrl: "/assets/views/profile/awards.htm", controller: "AwardsController"})
                .when("/profile/statistics", {templateUrl: "/assets/views/profile/statistics.htm", controller: "StatisticsController"})
                .when("/profile", {redirectTo: "/profile/awards"})
                .otherwise({redirectTo: "/discover"});
        }]);

    /**
     * Initialize the application
     */
    app.run(function ($rootScope, FavoriteSymbols, HeldSecurities, MySession, RecentSymbols, WebSockets) {
        $rootScope.FavoriteSymbols = FavoriteSymbols;
        $rootScope.HeldSecurities = HeldSecurities;
        $rootScope.MySession = MySession;
        $rootScope.RecentSymbols = RecentSymbols;

        // initialize the web socket service
        WebSockets.init();
    });

})();