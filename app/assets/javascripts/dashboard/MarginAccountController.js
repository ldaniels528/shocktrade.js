(function () {
    var app = angular.module('shocktrade');

    /**
     * Margin Account Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('MarginAccountController', ['$scope', '$log', '$timeout', 'toaster', 'MySession', 'ContestService',
        function ($scope, $log, $timeout, toaster, MySession, ContestService) {

            var interestRate = 0.15;
            var initialMargin = 0.50;
            var maintenanceMargin = 0.25;

            var attemptsLeft = 3;
            $scope.initMarginAccount = function () {
                $scope.investmentMarketValue = $scope.getInvestmentCost();

                // load the margin accounts market value
                ContestService.getMarginMarketValue(MySession.getContestID(), MySession.getUserID())
                    .success(function (contest) {
                        $scope.investmentMarketValue = contest.marginMarketValue;
                    })
                    .error(function (err) {
                        toaster.pop("error", "Failed to retrieve the Margin Account's market value", null);
                        if(--attemptsLeft > 0) {
                            $timeout($scope.initMarginAccount(), 15000);
                        }
                    })
            };

            $scope.getAsOfDate = function () {
                return MySession.getMarginAccount().asOfDate;
            };

            $scope.getBuyingPower = function () {
                return $scope.getCashFunds() / $scope.getInitialMargin();
            };

            $scope.getCashFunds = function () {
                return MySession.getMarginAccount().cashFunds || 0;
            };

            $scope.getInterestPaid = function () {
                return MySession.getMarginAccount().interestPaid || 0;
            };

            $scope.getInitialMargin = function () {
                return initialMargin;
            };

            $scope.getMaintenanceMargin = function () {
                return maintenanceMargin;
            };

            $scope.getInvestmentCost = function () {
                var positions = MySession.getPositions() || [];
                var totalValue = 0.00;
                angular.forEach(positions, function (p) {
                    if (p.accountType === 'MARGIN') {
                        totalValue += p.pricePaid * p.quantity;
                    }
                });
                return totalValue;
            };

            $scope.getInvestmentMarketValue = function () {
                return $scope.investmentMarketValue;
            };

            $scope.isAccountInGoodStanding = function () {
                return $scope.getCashFunds() >= $scope.getMaintenanceMarginAmount();
            };

            $scope.getMarginAccountEquity = function () {
                return $scope.getCashFunds() + (($scope.getInvestmentMarketValue() || $scope.getInvestmentCost()) - $scope.getInvestmentCost());
            };

            $scope.getMaintenanceMarginAmount = function () {
                var maintenanceAmount = ($scope.getInvestmentCost() - $scope.getMarginAccountEquity()) * $scope.getMaintenanceMargin();
                return maintenanceAmount > 0 ? maintenanceAmount : 0;
            };

            $scope.getMarginCallAmount = function() {
                return $scope.getMaintenanceMarginAmount() - $scope.getCashFunds(); // TODO round to nearest penny
            };

        }]);

})();