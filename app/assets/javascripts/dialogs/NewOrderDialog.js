/**
 * New Order Dialog Service and Controller
 * @author lawrence.daniels@gmail.com
 */
(function () {
    var app = angular.module('shocktrade');

    /**
     * New Order Dialog Singleton
     * @author lawrence.daniels@gmail.com
     */
    app.factory('NewOrderDialog', function ($http, $log, $modal, MySession) {
        var service = {};

        /**
         * Opens a new Order Entry Pop-up Dialog
         * @param params the given input parameters (e.g. { symbol: *, quantity: * })
         */
        service.popup = function (params) {
            // create an instance of the dialog
            var $modalInstance = $modal.open({
                controller: 'NewOrderDialogCtrl',
                templateUrl: 'new_order_dialog.htm',
                resolve: {
                    params: function () {
                        return params;
                    }
                }
            });

            $modalInstance.result.then(
                function (contest) {
                    MySession.setContest(contest);
                    //if (callback) callback(form)
                },
                function () {
                    $log.info('Modal dismissed at: ' + new Date());
                });
        };

        service.lookupQuote = function (symbol) {
            return $http.get("/api/quotes/cached/" + symbol);
        };

        return service;
    });

    /**
     * New Order Dialog Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('NewOrderDialogCtrl', ['$scope', '$log', '$modalInstance', 'toaster', 'params', 'ContestService', 'MySession', 'NewOrderDialog', 'PerksDialog', 'QuoteService',
        function ($scope, $log, $modalInstance, toaster, params, ContestService, MySession, NewOrderDialog, PerksDialog, QuoteService) {

            $scope.loading = false;
            $scope.processing = false;
            $scope.form = {
                emailNotify: true,
                symbol: (params.symbol || "AAPL"),
                quantity: params.quantity,
                accountType: params.accountType ? params.accountType : "CASH"
            };
            $scope.messages = [];
            $scope.quote = {symbol: $scope.form.symbol};

            ///////////////////////////////////////////////////////////////////////////
            //          Public Functions
            ///////////////////////////////////////////////////////////////////////////

            $scope.init = function () {
                $scope.orderQuote($scope.form.symbol);
            };

            $scope.autoCompleteSymbols = function (searchTerm) {
                return QuoteService.autoCompleteSymbols(searchTerm, 20)
                    .then(function (response) {
                        return response.data;
                    });
            };

            $scope.orderQuote = function (ticker) {
                // determine the symbol
                var symbol = null;
                if (ticker.symbol) {
                    symbol = ticker.symbol;
                }
                else {
                    var index = ticker.indexOf(' ');
                    symbol = (index == -1 ? ticker : ticker.substring(0, index - 1)).toUpperCase();
                }

                if (symbol && (symbol.trim().length > 0)) {
                    symbol = symbol.toUpperCase();
                    NewOrderDialog.lookupQuote(symbol)
                        .success(function (quote) {
                            $scope.quote = quote;
                            $scope.form.symbol = quote.symbol;
                            $scope.form.limitPrice = quote.lastTrade;
                            $scope.form.exchange = quote.exchange;
                        })
                        .error(function (err) {
                            $scope.messages.push("The order could not be processed (error code " + err.status + ")");
                        });
                }
            };

            $scope.getTotal = function (form) {
                var price = form.limitPrice ? parseFloat(form.limitPrice) : 0;
                var quantity = form.quantity ? parseFloat(form.quantity) : 0;
                var total = (price * quantity);
                return (total != 0) ? total : 0.00;
            };

            $scope.validate = function (form) {
                $scope.messages = [];

                // quantity must be numeric
                if (form.quantity && angular.isString(form.quantity)) form.quantity = parseInt(form.quantity);

                // perform the validations
                if (!form.accountType) $scope.messages.push("Please selected the account to use (Cash or Margin)");
                if (form.accountType == 'MARGIN' && !MySession.hasMarginAccount()) $scope.messages.push("You do not have a Margin Account (must buy the Perk)");
                if (!form.orderType) $scope.messages.push("No Order Type (BUY or SELL) specified");
                if (!form.priceType) $scope.messages.push("No Pricing Method specified");
                if (!form.orderTerm) $scope.messages.push("No Order Term specified");
                if (!form.quantity || form.quantity == 0) $scope.messages.push("No quantity specified");
                return $scope.messages.length == 0;
            };

            $scope.ok = function () {
                if ($scope.validate($scope.form)) {
                    $scope.processing = true;
                    var contestId = MySession.getContestID();
                    var playerId = MySession.getUserID();
                    $log.info("contestId = " + contestId + ", playerId = " + playerId + ", form = " + angular.toJson($scope.form));

                    ContestService.createOrder(contestId, playerId, $scope.form)
                        .success(function (contest) {
                            $scope.processing = false;
                            $modalInstance.close(contest);
                        })
                        .error(function (err) {
                            $scope.processing = false;
                            $scope.messages.push("The order could not be processed (error code " + err.status + ")");
                        });
                }
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

            ///////////////////////////////////////////////////////////////////////////
            //          Initialization
            ///////////////////////////////////////////////////////////////////////////

            (function () {
                // load the player's perks
                PerksDialog.getMyPerks()
                    .success(function (contest) {
                        $scope.form.perks = contest.perks || [];
                    })
                    .error(function () {
                        toaster.pop('error', 'Error retrieving perks', null)
                    })
            })();

        }]);

})();