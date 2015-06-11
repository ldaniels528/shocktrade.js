(function () {
    var app = angular.module('shocktrade');

    /**
     * Trading History Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('TradingHistoryController', ['$scope', '$cookieStore', '$interval', '$log', '$routeParams', '$timeout', 'toaster', 'QuoteService',
        function ($scope, $cookieStore, $interval, $log, $routeParams, $timeout, toaster, QuoteService) {
            $scope.tradingHistory = null;
            $scope.selectedTradingHistory = null;

            $scope.getTradingHistory = function() {
                return $scope.tradingHistory;
            };

            $scope.selectTradingHistory = function (t) {
                $scope.selectedTradingHistory = t;
            };

            $scope.hasSelectedTradingHistory = function () {
                return $scope.selectedTradingHistory != null;
            };

            $scope.isSelectedTradingHistory = function (t) {
                return $scope.selectedTradingHistory === t;
            };

            $scope.loadTradingHistory = function (symbol, callback) {
                QuoteService.getTradingHistory(symbol)
                    .success(function (results) {
                        $scope.tradingHistory = results;
                        if (callback) callback();
                    })
                    .error(function (response) {
                        toaster.pop('error', 'Error!', "Error loading trading history for " + symbol);
                        if (callback) callback();
                    });
            };

        }])

})();