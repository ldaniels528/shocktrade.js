angular
    .module('shocktrade')
    .controller('PlayPortfolioCtrl', ['$scope', '$location', '$log', '$timeout', 'MySession', 'ContestService', 'Errors', 'NewOrderDialog',
        function ($scope, $location, $log, $timeout, MySession, ContestService, Errors, NewOrderDialog) {

            $scope.newOrderPopup = function () {
                NewOrderDialog.popup($scope);
            };

            $scope.cancelOrder = function (contestId, playerId, orderId) {
                ContestService.deleteOrder(contestId, playerId, orderId)
                    .success(function (participant) {
                        $scope.participant = participant;
                        $scope.updateWithPricing(participant);
                    })
                    .error(function (response) {
                        Errors.addMessage("Failed to cancel order");
                    });
            };

            $scope.getCashAvailable = function (participant) {
                return (participant.fundsAvailable || 0) - $scope.getTotalBuyOrders(participant);
            };

            $scope.asOfDate = function (participant) {
                return participant.lastTradeTime ? participant.lastTradeTime : new Date();
            };

            $scope.getTotalOrders = function (participant) {
                return $scope.getTotalBuyOrders(participant) + $scope.getTotalSellOrders(participant);
            };

            $scope.getTotalEquity = function (participant) {
                return $scope.getTotalInvestment(participant) + (participant.fundsAvailable || 0);
            };

            $scope.getTotalInvestment = function (participant) {
                var positions = participant.positions || [];
                var total = 0;
                for (var n = 0; n < positions.length; n++) {
                    var p = positions[n];
                    total += p.price * p.quantity + p.commission;
                }
                return total;
            };

            $scope.getTotalBuyOrders = function (participant) {
                var orders = participant.orders || [];
                var total = 0;
                angular.forEach(orders, function (o) {
                    if (o.orderType == 'BUY') {
                        total += o.price * o.quantity + o.commission;
                    }
                });
                return total;
            };

            $scope.getTotalSellOrders = function (participant) {
                var orders = participant.orders || [];
                var total = 0;
                angular.forEach(orders, function (o) {
                    if (o.orderType == 'SELL') {
                        total += o.price * o.quantity + o.commission;
                    }
                });
                return total;
            };

            $scope.isMarketOrder = function (order) {
                return order.priceType == 'MARKET' || order.priceType == 'MARKET_ON_CLOSE';
            };

        }]);