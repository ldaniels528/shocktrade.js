(function () {
    var app = angular.module('shocktrade');

    /**
     * Margin Account Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('MarginAccountController', ['$scope', '$log', 'toaster', 'MySession',
        function ($scope, $log, toaster, MySession) {
            
            $scope.investmentValue = 15000.00;

            $scope.actions = [{
                label: "Deposit Funds",
                value: "DEPOSIT"
            }, {
                label: "Withdraw Funds",
                value: "WITHDRAW"
            }];

            $scope.init = function () {
                $log.info("margin = " + angular.toJson(MySession.getMarginAccount(), true));
            };

            $scope.getAsOfDate = function () {
                return MySession.getMarginAccount().asOfDate;
            };

            $scope.getBuyingPower = function () {
                return $scope.getDepositedFunds() / $scope.getInitialMargin();
            };

            $scope.getDepositedFunds = function () {
                return MySession.getMarginAccount().depositedFunds || 0;
            };

            $scope.getInterestRate = function () {
                return MySession.getMarginAccount().interestRate;
            };

            $scope.getInitialMargin = function () {
                return MySession.getMarginAccount().initialMargin;
            };

            $scope.getCashInvestedAmount = function () {
                return MySession.getMarginAccount().cashInvestedAmount || 0;
            };

            $scope.getInvestmentValue = function () {
                return $scope.investmentValue;
            };

            $scope.getMaintenanceMargin = function () {
                return MySession.getMarginAccount().maintenanceMargin;
            };

            $scope.getMarginFundsAvailable = function () {
                return $scope.getBuyingPower() - $scope.getCashInvestedAmount();
            };

            $scope.getNetMarginFundsAvailable = function () {
                return $scope.getMarginFundsAvailable() - $scope.getMinimumDepositAmount();
            };

            $scope.getMinimumDepositAmount = function () {
                return ($scope.getBuyingPower() - $scope.getMarginFundsAvailable()) * $scope.getMaintenanceMargin();
            };

        }]);

})();