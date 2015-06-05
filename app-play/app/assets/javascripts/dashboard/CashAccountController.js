/**
 * Cash Account Controller
 * @author lawrence.daniels@gmail.com
 */
(function() {
    var app = angular.module('shocktrade');

    /**
     * Cash Account Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('CashAccountController', ['$scope', '$log', 'toaster', 'MySession',
        function ($scope, $log, toaster, MySession) {

            /////////////////////////////////////////////////////////////////////
            //          Cash Account Functions
            /////////////////////////////////////////////////////////////////////

            $scope.asOfDate = function () {
                var participant = MySession.getParticipant();
                return participant.lastTradeTime || new Date();
            };

            $scope.getTotalOrders = function () {
                return $scope.getTotalBuyOrders() + $scope.getTotalSellOrders();
            };

            $scope.getTotalEquity = function () {
                return $scope.getTotalInvestment() + MySession.getFundsAvailable();
            };

            $scope.getTotalInvestment = function () {
                var total = 0;
                angular.forEach($scope.getPositions(), function (p) {
                    total += p.netValue;
                });
                return total;
            };

            $scope.getTotalBuyOrders = function () {
                var participant = MySession.getParticipant();
                var total = 0;
                if (participant != null) {
                    angular.forEach(participant.orders, function (o) {
                        if (o.orderType == 'BUY') {
                            total += o.price * o.quantity + o.commission;
                        }
                    });
                }
                return total;
            };

            $scope.getTotalSellOrders = function () {
                var participant = MySession.getParticipant();
                var total = 0;
                if (participant != null) {
                    angular.forEach(participant.orders, function (o) {
                        if (o.orderType == 'SELL') {
                            total += o.price * o.quantity + o.commission;
                        }
                    });
                }
                return total;
            };

        }]);

})();