/**
 * Retrieves the current stock market status
 * @author lawrence.daniels@gmail.com
 */
angular
    .module('shocktrade')
    .factory('MarketStatus', function ($http, $log) {
        return {
            getMarketStatus: function (callback) {
                // {"stateChanged":false,"active":false,"sysTime":1392092448795,"delay":-49848795,"start":1392042600000,"end":1392066000000}
                $http({
                    method: 'GET',
                    url: "/api/tradingClock/status/0"
                }).success(function (response) {
                    if (callback) {
                        callback(response);
                    }
                }).error(function (response) {
                    $log.error(angular.toJson(response));
                });
            }
        };
    });