(function () {
    var app = angular.module('shocktrade');

    /**
     * Retrieves the current stock market status
     * @author lawrence.daniels@gmail.com
     */
    app.factory('MarketStatus', function ($http) {
        return {
            getMarketStatus: function () {
                // {"stateChanged":false,"active":false,"sysTime":1392092448795,"delay":-49848795,"start":1392042600000,"end":1392066000000}
                return $http.get("/api/tradingClock/status/0");
            }
        };
    });

})();