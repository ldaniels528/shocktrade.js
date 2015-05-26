(function () {
    var app = angular.module('shocktrade');

    /**
     * Portfolio Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('PortfolioController', ['$scope', '$cookieStore', '$log', '$timeout', 'toaster', 'MySession', 'ContestService', 'NewOrderDialog', 'QuoteService',
        function ($scope, $cookieStore, $log, $timeout, toaster, MySession, ContestService, NewOrderDialog, QuoteService) {

            $scope.selectedClosedOrder = null;
            $scope.selectedOrder = null;
            $scope.selectedPosition = null;

            $scope.portfolioTabs = [{
                "name": "Chat",
                "icon": "fa-comment-o",
                "path": "/assets/views/dashboard/chat.htm",
                "active": false,
                "init": function () {
                },
                "isLocked": function () {
                    return false;
                }
            }, {
                "name": "Positions",
                "icon": "fa-list-alt",
                "path": "/assets/views/dashboard/positions.htm",
                "active": false,
                "init": function () {
                    // TODO enrich positions here
                },
                "isLocked": function () {
                    return MySession.getContestStatus() !== 'ACTIVE';
                }
            }, {
                "name": "Open Orders",
                "icon": "fa-folder-open-o",
                "path": "/assets/views/dashboard/orders_active.htm",
                "active": false,
                "init": function () {
                    // TODO enrich orders here
                    angular.forEach($scope.getActiveOrders(), function(o) {
                        $log.info("portfolioTabs: order => " + angular.toJson(o));
                    });
                },
                "isLocked": function () {
                    return MySession.getContestStatus() !== 'ACTIVE';
                }
            }, {
                "name": "Closed Orders",
                "icon": "fa-folder-o",
                "path": "/assets/views/dashboard/orders_closed.htm",
                "active": false,
                "init": function () {
                },
                "isLocked": function () {
                    return false;
                }
            }, {
                "name": "Performance",
                "icon": "fa-bar-chart-o",
                "path": "/assets/views/dashboard/performance.htm",
                "active": false,
                "init": function () {
                },
                "isLocked": function () {
                    return false;
                }
            }, {
                "name": "Exposure",
                "icon": "fa-pie-chart",
                "path": "/assets/views/dashboard/exposure.htm",
                "active": false,
                "init": function () {
                },
                "isLocked": function () {
                    return MySession.getContestStatus() !== 'ACTIVE';
                }
            }];

            /////////////////////////////////////////////////////////////////////
            //          Active Order Functions
            /////////////////////////////////////////////////////////////////////

            $scope.getActiveOrders = function () {
                enrichOrders(MySession.getParticipant());
                var orders = MySession.getOrders() || [];
                return orders.filter(function(o) {
                    return o.accountType === $scope.getAccountType();
                });
            };

            $scope.cancelOrder = function (contestId, playerId, orderId) {
                ContestService.deleteOrder(contestId, playerId, orderId)
                    .success(function (contest) {
                        MySession.setContest(contest);
                    })
                    .error(function (err) {
                        toaster.pop('error', 'Error!', "Failed to cancel order");
                    });
            };

            $scope.isMarketOrder = function (order) {
                return order.priceType == 'MARKET' || order.priceType == 'MARKET_ON_CLOSE';
            };

            $scope.isOrderSelected = function () {
                return $scope.getActiveOrders().length && $scope.selectedOrder != null;
            };

            $scope.selectOrder = function (order) {
                $scope.selectedOrder = order;
            };

            $scope.toggleSelectedOrder = function () {
                $scope.selectedOrder = null;
            };

            $scope.popupNewOrderDialog = function (params) {
                // were the parameters passed?
                params = params || {};

                var symbol = $cookieStore.get('symbol');
                if (!symbol) symbol = QuoteService.lastSymbol;
                params.symbol = symbol;

                NewOrderDialog.popup(params);
            };

            /////////////////////////////////////////////////////////////////////
            //          Closed Order Functions
            /////////////////////////////////////////////////////////////////////

            $scope.getClosedOrders = function () {
                var closedOrders = MySession.getOrderHistory() || [];
                return closedOrders.filter(function(o) {
                    return $scope.getClosedOrders.length && o.accountType === $scope.getAccountType();
                });
            };

            $scope.isClosedOrderSelected = function () {
                return $scope.selectedClosedOrder != null;
            };

            $scope.selectClosedOrder = function (closeOrder) {
                $scope.selectedClosedOrder = closeOrder;
            };

            $scope.toggleSelectedClosedOrder = function () {
                $scope.selectedClosedOrder = null;
            };

            $scope.orderCost = function (o) {
                return o.price * o.quantity + o.commission;
            };

            /////////////////////////////////////////////////////////////////////
            //          Performance Functions
            /////////////////////////////////////////////////////////////////////

            $scope.getPerformances = function () {
                return MySession.getPerformance() || [];
            };

            $scope.isPerformanceSelected = function () {
                return $scope.getPerformances().length && $scope.selectedPerformance != null;
            };

            $scope.selectPerformance = function (performance) {
                $scope.selectedPerformance = performance;
            };

            $scope.toggleSelectedPerformance = function () {
                $scope.selectedPerformance = null;
            };

            $scope.cost = function (tx) {
                return tx.pricePaid * tx.quantity + tx.commissions;
            };

            $scope.soldValue = function (tx) {
                return tx.priceSold * tx.quantity;
            };

            $scope.proceeds = function (tx) {
                return $scope.soldValue(tx) - $scope.cost(tx);
            };

            $scope.gainLoss = function (tx) {
                return 100.0 * ($scope.proceeds(tx) / $scope.cost(tx));
            };

            /////////////////////////////////////////////////////////////////////
            //          Position Functions
            /////////////////////////////////////////////////////////////////////

            $scope.getPositions = function () {
                enrichPositions(MySession.getParticipant());
                var positions = MySession.getPositions() || [];
                return positions.filter(function(p) {
                    return p.accountType === $scope.getAccountType();
                });
            };

            $scope.isPositionSelected = function () {
                return $scope.getPositions().length && $scope.selectedPosition != null;
            };

            $scope.selectPosition = function (position) {
                $scope.selectedPosition = position;
            };

            $scope.sellPosition = function (symbol, quantity) {
                NewOrderDialog.popup({symbol: symbol, quantity: quantity});
            };

            $scope.toggleSelectedPosition = function () {
                $scope.selectedPosition = null;
            };

            $scope.tradingStart = function () {
                return (new Date()).getTime(); // TODO replace with service call
            };

            /////////////////////////////////////////////////////////////////////
            //          Private Functions
            /////////////////////////////////////////////////////////////////////

            function enrichOrders(participant) {
                if (!MySession.participantIsEmpty()) {
                    if (!participant.enrichedOrders) {
                        participant.enrichedOrders = true;
                        ContestService.getEnrichedOrders(MySession.getContestID(), participant.OID())
                            .success(function (enrichedOrders) {
                                MySession.getParticipant().orders = enrichedOrders;
                            })
                            .error(function (err) {
                                toaster.pop('error', 'Error!', "Error loading enriched orders");
                            });
                    }
                }
            }

            function enrichPositions(participant) {
                if (!MySession.participantIsEmpty()) {
                    if (!participant.enrichedPositions) {
                        participant.enrichedPositions = true;
                        ContestService.getEnrichedPositions(MySession.getContestID(), participant.OID())
                            .success(function (enrichedPositions) {
                                MySession.getParticipant().positions = enrichedPositions;
                            })
                            .error(function (err) {
                                toaster.pop('error', 'Error!', "Error loading enriched positions");
                            });
                    }
                }
            }

            function resetOrders() {
                $scope.selectedOrder = null;
                $scope.selectedClosedOrder = null;
            }

            function resetPositions() {
                $scope.selectedPosition = null;
                $scope.selectedPerformance = null;
            }

            //////////////////////////////////////////////////////////////////////
            //              Watch Event Listeners
            //////////////////////////////////////////////////////////////////////

            $scope.$on("contest_selected", function (event, contest) {
                $log.info("[Portfolio] Contest '" + contest.name + "' selected");
                resetOrders();
                resetPositions();
            });

            $scope.$on("contest_updated", function (event, contest) {
                $log.info("[Portfolio] Contest '" + contest.name + "' updated");
                resetOrders();
                resetPositions();
            });

            $scope.$on("orders_updated", function (event, contest) {
                $log.info("[Portfolio] Orders for Contest '" + contest.name + "' updated");
                resetOrders();
            });

            $scope.$on("participant_updated", function (event, contest) {
                $log.info("[Portfolio] Orders for Contest '" + contest.name + "' updated");
                resetPositions();
            });

        }]);

})();