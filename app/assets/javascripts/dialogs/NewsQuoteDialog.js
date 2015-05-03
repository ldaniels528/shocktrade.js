(function () {
    // get the application reference
    var app = angular.module('shocktrade');

    /**
     * Compose Message Dialog Singleton
     * @author lawrence.daniels@gmail.com
     */
    app.factory('NewsQuoteDialog', function ($http, $log, $modal) {
        var service = {};

        /**
         * Popups the Compose Message Dialog
         */
        service.popup = function (params) {
            // create an instance of the dialog
            var $modalInstance = $modal.open({
                templateUrl: 'news_quote.htm',
                controller: 'NewsQuoteCtrl',
                resolve: {
                    symbol: function () {
                        return params.symbol;
                    }
                }
            });

            $modalInstance.result.then(
                function (quote) {
                    $log.info("quote = " + angular.toJson(quote));
                },
                function () {
                    $log.info('Modal dismissed at: ' + new Date());
                });
        };

        return service;
    });

    /**
     * Compose Message Dialog Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('NewsQuoteDialogCtrl', ['$scope', '$log', '$modalInstance', 'ContestService', 'QuoteService', 'symbol',
        function ($scope, $log, $modalInstance, ContestService, QuoteService, symbol) {
            $scope.quote = {};

            $scope.init = function (symbol) {
                var scope = angular.element($("#NewsBlock")).scope();
                ContestService.orderQuote(symbol).then(
                    function (data) {
                        $scope.quote = data;
                    },
                    function (response) {
                        $log.error("Error: " + response.status);
                    });
            };

            $scope.ok = function () {
                $modalInstance.close($scope.form);
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        }]);

})();