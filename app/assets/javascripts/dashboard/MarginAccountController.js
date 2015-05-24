(function() {
    var app = angular.module('shocktrade');

    /**
     * Margin Account Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('MarginAccountController', ['$scope', '$log', 'toaster', 'MySession',
        function ($scope, $log, toaster, MySession) {

            $scope.depositedFunds = 10000.00;
            $scope.initialMargin = 0.50;
            $scope.maintenanceMargin = 0.30;
            $scope.investedAmount = 12000.00;
            $scope.investmentValue = 15000.00;

            $scope.actions = [{
                label: "Deposit Funds",
                value: "DEPOSIT"
            },{
                label: "Withdraw Funds",
                value: "WITHDRAW"
            }];

            $scope.init = function () {

            };

            $scope.getBuyingPower = function () {
                return $scope.depositedFunds / $scope.initialMargin;
            };

            $scope.getDepositedFunds = function () {
                return $scope.depositedFunds;
            };

            $scope.getInitialMargin = function() {
                return $scope.initialMargin;
            };

            $scope.getInvestedAmount = function () {
                return $scope.investedAmount;
            };

            $scope.getInvestmentValue = function () {
                return $scope.investmentValue;
            };

            $scope.getMaintenanceMargin = function () {
                return $scope.maintenanceMargin;
            };

            $scope.getMarginFundsAvailable = function () {
                return $scope.getBuyingPower() - $scope.investedAmount;
            };

            $scope.getNetMarginFundsAvailable = function () {
                return $scope.getMarginFundsAvailable() - $scope.getMinimumDepositAmount();
            };

            $scope.getMinimumDepositAmount = function() {
                return ($scope.getBuyingPower() - $scope.getMarginFundsAvailable()) * $scope.maintenanceMargin;
            };

        }]);

})();