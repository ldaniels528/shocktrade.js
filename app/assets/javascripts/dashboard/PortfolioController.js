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
                    return MySession.getContestStatus() !== 'ACTIVE';
                }
            }, {
                "name": "Perks",
                "icon": "fa-gift",
                "path": "/assets/views/dashboard/perks.htm",
                "active": false,
                "init": function () {
                },
                "isLocked": function () {
                    return MySession.getContestStatus() !== 'ACTIVE';
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
                    return true;
                }
            }, {
                "name": "Performance",
                "icon": "fa-bar-chart-o",
                "path": "/assets/views/dashboard/performance.htm",
                "active": false,
                "init": function () {
                },
                "isLocked": function () {
                    return true;
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
            //          Participant Functions
            /////////////////////////////////////////////////////////////////////

            $scope.isRankingsShown = function () {
                return !MySession.getContest().rankingsHidden;
            };

            $scope.toggleRankingsShown = function () {
                var contest = MySession.getContest();
                contest.rankingsHidden = !contest.rankingsHidden;
            };

            $scope.getRankings = function () {
                if (MySession.contestIsEmpty()) return [];
                else {
                    var rankings = ContestService.getPlayerRankings(MySession.getContest(), MySession.getUserName());
                    return rankings.participants;
                }
            };

            /////////////////////////////////////////////////////////////////////
            //          Selected Active Order Functions
            /////////////////////////////////////////////////////////////////////

            $scope.getActiveOrders = function () {
                enrichOrders(MySession.getParticipant());
                return MySession.getOrders();
            };

            $scope.cancelOrder = function (contestId, playerId, orderId) {
                ContestService.deleteOrder(contestId, playerId, orderId)
                    .success(function (participant) {
                        updateWithPricing(participant);
                    })
                    .error(function (err) {
                        toaster.pop('error', 'Error!', "Failed to cancel order");
                    });
            };

            $scope.isOrderSelected = function () {
                return $scope.selectedOrder != null;
            };

            $scope.selectOrder = function (order) {
                $scope.selectedOrder = order;
            };

            $scope.toggleSelectedOrder = function () {
                $scope.selectedOrder = null;
            };

            $scope.popupNewOrderDialog = function () {
                NewOrderDialog.popup({symbol: QuoteService.lastSymbol});
            };

            /////////////////////////////////////////////////////////////////////
            //          Selected Closed Order Functions
            /////////////////////////////////////////////////////////////////////

            $scope.getClosedOrders = function () {
                return MySession.getOrderHistory();
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
                return MySession.getPerformance();
            };

            $scope.isPerformanceSelected = function () {
                return $scope.selectedPerformance != null;
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
                return MySession.getPositions();
            };

            $scope.isPositionSelected = function () {
                return $scope.selectedPosition != null;
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

            /////////////////////////////////////////////////////////////////////
            //          Summary Functions
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

            $scope.isMarketOrder = function (order) {
                return order.priceType == 'MARKET' || order.priceType == 'MARKET_ON_CLOSE';
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

            $scope.$on("positions_updated", function (event, contest) {
                $log.info("[Portfolio] Orders for Contest '" + contest.name + "' updated");
                resetPositions();
            });

        }]);

})();