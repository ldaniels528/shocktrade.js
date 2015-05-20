(function () {
    var app = angular.module('shocktrade');

    /**
     * Portfolio Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('PortfolioController', ['$scope', '$log', '$timeout', 'toaster', 'MySession', 'ContestService', 'NewOrderDialog', 'QuoteService',
        function ($scope, $log, $timeout, toaster, MySession, ContestService, NewOrderDialog, QuoteService) {

            var positions = [];
            var orders = [];
            var orderHistory = [];
            var performance = [];

            $scope.selectedClosedOrder = null;
            $scope.selectedOrder = null;
            $scope.selectedPosition = null;
            $scope.selection = {
                exposure: "sector",
                performance: "gains"
            };

            $scope.portfolioTabs = [{
                "name": "Chat",
                "icon": "fa-comment-o",
                "path": "/assets/views/dashboard/chat.htm",
                "active": false,
                "init": function (contest) {
                    // TODO enrich orders here
                },
                "isLocked": function (contest) {
                    return contest.status !== 'ACTIVE';
                }
            }, {
                "name": "Positions",
                "icon": "fa-list-alt",
                "path": "/assets/views/dashboard/positions.htm",
                "active": false,
                "init": function (contest) {
                    // TODO enrich positions here
                },
                "isLocked": function (contest) {
                    return contest.status !== 'ACTIVE';
                }
            }, {
                "name": "Open Orders",
                "icon": "fa-folder-open-o",
                "path": "/assets/views/dashboard/orders_active.htm",
                "active": false,
                "init": function (contest) {
                    // TODO enrich orders here
                },
                "isLocked": function (contest) {
                    return contest.status !== 'ACTIVE';
                }
            }, {
                "name": "Closed Orders",
                "icon": "fa-folder-o",
                "path": "/assets/views/dashboard/orders_closed.htm",
                "active": false,
                "init": function (contest) {
                    // TODO enrich orders here
                },
                "isLocked": function (contest) {
                    return contest.status !== 'ACTIVE';
                }
            }, {
                "name": "Performance",
                "icon": "fa-bar-chart-o",
                "path": "/assets/views/dashboard/performance.htm",
                "active": false,
                "init": function (contest) {
                    // TODO enrich orders here
                },
                "isLocked": function (contest) {
                    return contest.status !== 'ACTIVE';
                }
            }, {
                "name": "Exposure",
                "icon": "fa-pie-chart",
                "path": "/assets/views/dashboard/exposure.htm",
                "active": false,
                "init": function (contest) {
                    // TODO enrich orders here
                },
                "isLocked": function (contest) {
                    return contest.status !== 'ACTIVE';
                }
            }];

            /////////////////////////////////////////////////////////////////////
            //          Participant Functions
            /////////////////////////////////////////////////////////////////////

            $scope.isRankingsShown = function (contest) {
                return !contest.rankingsHidden;
            };

            $scope.toggleRankingsShown = function (contest) {
                contest.rankingsHidden = !contest.rankingsHidden;
            };

            $scope.getRankings = function (contest) {
                if (!contest) return [];
                else if (!contest.rankings) {
                    contest.rankings = [];
                    $log.info("Loading rankings....");
                    ContestService.getRankings(contest.OID())
                        .success(function (rankings) {
                            contest.rankings = rankings;
                        })
                        .error(function (response) {
                            toaster.pop('error', 'Error!', "Error loading play rankings");
                            $log.error(response.error)
                        });
                }
                return contest.rankings;
            };

            $scope.getRankingByFBID = function (contest, facebookID) {
                if (!contest.myRanking) {
                    for (var n = 0; n < (contest.rankings || []).length; n++) {
                        if (contest.rankings[n].facebookID === facebookID) {
                            contest.myRanking = contest.rankings[n];
                            return contest.myRanking;
                        }
                    }
                }
                return contest.myRanking;
            };

            /////////////////////////////////////////////////////////////////////
            //          Selected Active Order Functions
            /////////////////////////////////////////////////////////////////////

            $scope.getActiveOrders = function (contest) {
                return orders;
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

            $scope.getClosedOrders = function (contest) {
                return orderHistory;
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

            $scope.getPerformances = function (contest) {
                return performance;
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

            $scope.getPositions = function (contest) {
                return positions;
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

            $scope.getCashAvailable = function (contest) {
                return ContestService.getCashAvailable(contest, MySession.getUserID());
            };

            $scope.asOfDate = function (contest) {
                var participant = getParticipant(contest);
                return participant && participant.lastTradeTime ? participant.lastTradeTime : new Date();
            };

            $scope.getTotalOrders = function (contest) {
                return $scope.getTotalBuyOrders(contest) + $scope.getTotalSellOrders(contest);
            };

            $scope.getTotalEquity = function (contest) {
                return $scope.getTotalInvestment(contest) + $scope.getCashAvailable(contest);
            };

            $scope.getTotalInvestment = function (contest) {
                var participant = getParticipant(contest);
                var total = 0;
                if (participant != null) {
                    angular.forEach(participant.positions, function (p) {
                        total += p.netValue;
                    });
                }
                return total;
            };

            $scope.getTotalBuyOrders = function (contest) {
                var participant = getParticipant(contest);
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

            $scope.getTotalSellOrders = function (contest) {
                var participant = getParticipant(contest);
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

            function getParticipant(contest) {
                return ContestService.findPlayerByID(contest, MySession.getUserID());
            }

            function contestUpdated(contest) {
                if (contest) {
                    ordersUpdated(contest);
                    positionsUpdated(contest);
                    performanceUpdated(contest);
                }
            }

            function ordersUpdated(contest) {
                if (contest) {
                    $scope.selectedOrder = null;
                    $scope.selectedClosedOrder = null;
                    var participant = getParticipant(contest);
                    if (participant) {
                        orders = participant ? participant.orders : [];
                        orderHistory = participant ? participant.orderHistory : [];
                        enrichOrders(contest, participant);
                    }
                }
            }

            function positionsUpdated(contest) {
                if(contest) {
                    $scope.selectedPosition = null;
                    var participant = getParticipant(contest);
                    if (participant) {
                        positions = participant ? participant.positions : [];
                        enrichPositions(contest, participant);
                    }
                }
            }

            function performanceUpdated(contest) {
                if(contest) {
                    $scope.selectedPerformance = null;
                    var participant = getParticipant(contest);
                    if (participant) {
                        performance = participant ? participant.performance : [];
                    }
                }
            }

            function enrichOrders(contest, participant) {
                // enrich the orders
                if (!participant.enrichedOrders) {
                    participant.enrichedOrders = true;
                    ContestService.getEnrichedOrders(contest.OID(), participant.OID())
                        .success(function (enrichedOrders) {
                            $log.info("Loaded enriched orders " + angular.toJson(enrichedOrders));
                            orders = enrichedOrders;
                        })
                        .error(function (err) {
                            toaster.pop('error', 'Error!', "Error loading enriched orders");
                        });
                }
            }

            function enrichPositions(contest, participant) {
                // enrich the positions
                if (!participant.enrichedPositions) {
                    participant.enrichedPositions = true;
                    ContestService.getEnrichedPositions(contest.OID(), participant.OID())
                        .success(function (enrichedPositions) {
                            $log.info("Loaded enriched positions " + angular.toJson(enrichedPositions));
                            positions = enrichedPositions;
                        })
                        .error(function (err) {
                            toaster.pop('error', 'Error!', "Error loading enriched positions");
                        });
                }
            }

            function enrichParticipant(contest, participant) {
                if (contest && participant) {

                    // enrich the orders
                    enrichOrders(contest, participant);

                    // enrich the positions
                    enrichPositions(contest, participant);
                }
            }

            function reset() {
                $scope.selectedClosedOrder = null;
                $scope.selectedOrder = null;
                $scope.selectedPosition = null;
                $scope.selectedPerformance = null;
            }

            //////////////////////////////////////////////////////////////////////
            //              Watch Event Listeners
            //////////////////////////////////////////////////////////////////////

            /**
             * Listen for contest update events
             */
            $scope.$on("contest_updated", function (event, contest) {
                $log.info("[Portfolio] Contest '" + contest.name + "' updated");
                contestUpdated(contest);
            });

            /**
             * Listen for contest update events
             */
            $scope.$on("orders_updated", function (event, contest) {
                $log.info("[Portfolio] Orders for Contest '" + contest.name + "' updated");
                ordersUpdated(contest);
            });

            /**
             * Listen for contest update events
             */
            $scope.$on("positions_updated", function (event, contest) {
                $log.info("[Portfolio] Orders for Contest '" + contest.name + "' updated");
                positionsUpdated(contest);
            });

            /**
             * Watch for contest change events
             */
            $scope.$watch(MySession.contest, function () {
                contestUpdated(MySession.contest);
            }, true);

        }]);

})();