(function () {
    var app = angular.module('shocktrade');

    /**
     * Portfolio Controller
     */
    app.controller('PortfolioController', ['$scope', '$log', '$timeout', 'toaster', 'MySession', 'ContestService', 'NewOrderDialog', 'QuoteService',
        function ($scope, $log, $timeout, toaster, MySession, ContestService, NewOrderDialog, QuoteService) {

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

            $scope.getParticipant = function (contest) {
                var participant = ContestService.findPlayerByID(contest, MySession.getUserID());
                if (participant) {
                    enrichParticipant(contest, participant);
                    return participant;
                }
            };

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

            function enrichParticipant(contest, participant) {
                if (contest && participant) {
                    // enrich the orders
                    ContestService.getEnrichedOrders(contest.OID(), participant.OID())
                        .success(function (response) {
                            var enrichedOrders = response;
                            $log.info("Loaded enriched orders " + angular.toJson(enrichedOrders));
                            participant.orders = enrichedOrders;
                        })
                        .error(function (err) {
                            toaster.pop('error', 'Error!', "Error loading enriched orders");
                        });

                    // enrich the positions
                    ContestService.getEnrichedPositions(contest.OID(), participant.OID())
                        .success(function (response) {
                            var enrichedPositions = response;
                            $log.info("Loaded enriched positions " + angular.toJson(enrichedPositions));
                            participant.positions = enrichedPositions;
                        })
                        .error(function (err) {
                            toaster.pop('error', 'Error!', "Error loading enriched positions");
                        });
                }
            }

            /////////////////////////////////////////////////////////////////////
            //          Selected Active Order Functions
            /////////////////////////////////////////////////////////////////////

            $scope.getActiveOrders = function (contest) {
                var participant = $scope.getParticipant(contest);
                return participant ? (participant.orders || []) : [];
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
                var participant = $scope.getParticipant(contest);
                return participant ? (participant.orderHistory || []) : [];
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
                var participant = $scope.getParticipant(contest);
                return participant ? (participant.performance || []) : [];
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
                var participant = $scope.getParticipant(contest);
                return participant ? (participant.positions || []) : [];
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
                var participant = $scope.getParticipant(contest);
                return participant && participant.lastTradeTime ? participant.lastTradeTime : new Date();
            };

            $scope.getTotalOrders = function (contest) {
                return $scope.getTotalBuyOrders(contest) + $scope.getTotalSellOrders(contest);
            };

            $scope.getTotalEquity = function (contest) {
                return $scope.getTotalInvestment(contest) + $scope.getCashAvailable(contest);
            };

            $scope.getTotalInvestment = function (contest) {
                var participant = $scope.getParticipant(contest);
                var total = 0;
                if (participant != null) {
                    angular.forEach(participant.positions, function (p) {
                        total += p.netValue;
                    });
                }
                return total;
            };

            $scope.getTotalBuyOrders = function (contest) {
                var participant = $scope.getParticipant(contest);
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
                var participant = $scope.getParticipant(contest);
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
            //          Chart Functions
            /////////////////////////////////////////////////////////////////////

            $scope.getExposureChart = function (target) {
                $scope.getChart(MySession.getContestID(), MySession.getUserName(), $scope.selection.exposure, target);
            };

            $scope.getPerformanceChart = function (target) {
                $scope.getChart(MySession.getContestID(), MySession.getUserName(), $scope.selection.performance, target);
            };

            $scope.getChart = function (contestId, participantName, chartName, target) {
                if ((contestId && contestId != "") && (participantName && participantName != "")) {
                    // load the chart representing the securities
                    ContestService.getChart(contestId, participantName, chartName).then(
                        function (exposureData) {
                            // create the chart title & sub-title
                            var subTitle = chartName.capitalize();
                            var title = (chartName == "gains" || chartName == "losses") ? "Performance " + subTitle : subTitle + " Exposure";

                            // construct the chart
                            var exposure = new AmCharts.AmPieChart();
                            exposure.colors = ["#00dd00", "#ff00ff", "#00ffff", "#885500", "#0044ff", "#888800"];
                            exposure.addTitle(title, 12);
                            exposure.dataProvider = exposureData;
                            exposure.titleField = "title";
                            exposure.valueField = "value";
                            exposure.sequencedAnimation = true;
                            exposure.startEffect = "elastic";
                            exposure.startDuration = 2;
                            exposure.innerRadius = "30%";
                            exposure.labelRadius = 15;

                            // 3D chart
                            exposure.depth3D = 10;
                            exposure.angle = 15;
                            exposure.write(target);
                        },
                        function (err) {
                            $log.info("Error: " + angular.toJson(err));
                        });
                }
            };

            //////////////////////////////////////////////////////////////////////
            //              Watch Event Listeners
            //////////////////////////////////////////////////////////////////////

            function reset() {
                $scope.selectedClosedOrder = null;
                $scope.selectedOrder = null;
                $scope.selectedPosition = null;
            }

            /**
             * Listen for contest update events
             */
            $scope.$on("contest_updated", function (event, contest) {
                $log.info("[Portfolio] Contest '" + contest.name + "' updated");
                reset();
            });

            /**
             * Listen for contest update events
             */
            $scope.$on("orders_updated", function (event, contest) {
                $log.info("[Portfolio] Orders for Contest '" + contest.name + "' updated");
                reset();
            });

            /**
             * Listen for contest update events
             */
            $scope.$on("positions_updated", function (event, contest) {
                $log.info("[Portfolio] Orders for Contest '" + contest.name + "' updated");
                reset();
            });

            /**
             * Watch for contest change events
             */
            $scope.$watch(MySession.contest, function () {
                reset();
            }, true);

        }]);

})();