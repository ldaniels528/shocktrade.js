/**
 * Transfer Funds Dialog Service and Controller
 * @author lawrence.daniels@gmail.com
 */
(function () {
    var app = angular.module('shocktrade');

    /**
     * Transfer Funds Dialog Singleton
     * @author lawrence.daniels@gmail.com
     */
    app.factory('TransferFundsDialog', function ($http, $log, $modal) {
        var service = {};

        /**
         * Transfer Funds pop-up dialog
         */
        service.popup = function (params) {

            var modalInstance = $modal.open({
                templateUrl: 'transfer_funds_dialog.htm',
                controller: 'TransferFundsDialogController',
                resolve: {
                    params: function () {
                        return params;
                    }
                }
            });

            modalInstance.result.then(function (response) {
                $log.info("TransferFundsDialog: response = " + angular.toJson(response));
                if (params.success) {
                    params.success(response);
                }

            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };

        service.transferFunds = function (contestId, playerId, form) {
            return $http.post("/api/contest/" + contestId + "/margin/" + playerId, form);
        };

        return service;
    });

    /**
     * Transfer Funds Dialog Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('TransferFundsDialogController', ['$scope', '$log', '$modalInstance', 'TransferFundsDialog', 'MySession',
        function ($scope, $log, $modalInstance, TransferFundsDialog, MySession) {

            $scope.messages = [];
            $scope.investedAmount = 0.00; // TODO get actual values for these
            $scope.investmentValue = 0.00;
            $scope.actions = [{
                "label": "Cash to Margin Account",
                "source": "CASH"
            }, {
                "label": "Margin Account to Cash",
                "source": "MARGIN"
            }];

            $scope.form = {
                "cashFunds": MySession.getMarginAccount().cashFunds,
                "initialMargin": MySession.getMarginAccount().initialMargin,
                "fundsAvailable": MySession.getFundsAvailable(),
                "action": null,
                "amount": null
            };

            $scope.init = function () {
                // TODO compute the net value of the stock in the margin account
            };

            $scope.accept = function (form) {
                if (isValidated(form)) {
                    TransferFundsDialog.transferFunds(MySession.getContestID(), MySession.getUserID(), form)
                        .success(function (response) {
                            $modalInstance.close(response);
                        })
                        .error(function (err) {
                            $scope.messages.push("Failed to deposit funds")
                        });
                }
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

            function isValidated(form) {
                $scope.messages = [];

                // make sure the action is defined
                if (!form.action) {
                    $scope.messages.push("Please select an Action");
                }

                // validate the amount
                if (!form.amount) {
                    $scope.messages.push("Please enter the desired amount");
                }
                else if (form.action === 'DEPOSIT' && form.amount > form.fundsAvailable) {
                    $scope.messages.push("Insufficient funds in your cash account to complete the request")
                }
                else if (form.action === 'WITHDRAW' && form.amount > form.cashFunds) {
                    $scope.messages.push("Insufficient funds in your margin account to complete the request")
                }

                return $scope.messages.length === 0;
            }

        }]);


})();