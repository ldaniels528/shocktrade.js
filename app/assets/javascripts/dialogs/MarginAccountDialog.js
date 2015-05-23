/**
 * Margin Account Dialog Service and Controller
 * @author lawrence.daniels@gmail.com
 */
(function () {
    var app = angular.module('shocktrade');

    /**
     * Margin Account Dialog Singleton
     * @author lawrence.daniels@gmail.com
     */
    app.factory('MarginAccountDialog', function ($http, $log, $modal) {
        var service = {};

        /**
         * Margin Account pop-up dialog
         */
        service.popup = function (params) {

            var modalInstance = $modal.open({
                templateUrl: 'margin_acct_dialog.htm',
                controller: 'MarginAccountDialogController',
                resolve: {
                    params: function () {
                        return params;
                    }
                }
            });

            modalInstance.result.then(function (response) {
                $log.info("response = " + angular.toJson(response));
            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };

        return service;
    });

    /**
     * Margin Account Dialog Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('MarginAccountDialogController', ['$scope', '$log', '$modalInstance', 'MySession',
        function ($scope, $log, $modalInstance, MySession) {

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

            $scope.init = function () {

            };

            $scope.accept = function () {
                var result = [];
                $modalInstance.close(result);
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

        }]);


})();