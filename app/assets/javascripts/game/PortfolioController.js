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
                "name": "Positions",
                "imageURL": "/assets/images/objects/position.png",
                "path": "/assets/views/play/portfolio/positions.htm",
                "active": false,
                "init": function (contest) {
                    // TODO enrich positions here
                },
                "isLocked": function (contest) {
                    return contest.status !== 'ACTIVE';
                }
            }, {
                "name": "Open Orders",
                "imageURL": "/assets/images/objects/portfolio_query.png",
                "path": "/assets/views/play/portfolio/orders_active.htm",
                "active": false,
                "init": function (contest) {
                    // TODO enrich orders here
                },
                "isLocked": function (contest) {
                    return contest.status !== 'ACTIVE';
                }
            }, {
                "name": "Closed Orders",
                "imageURL": "/assets/images/objects/portfolio_header.png",
                "path": "/assets/views/play/portfolio/orders_closed.htm",
                "active": false,
                "init": function (contest) {
                    // TODO enrich orders here
                },
                "isLocked": function (contest) {
                    return contest.status !== 'ACTIVE';
                }
            }, {
                "name": "Messages",
                "imageURL": "/assets/images/objects/chat.png",
                "path": "/assets/views/play/portfolio/chat.htm",
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
                if ($scope.participant == null) {
                    $scope.participant = $scope.findPlayerByID(contest, MySession.getUserID());
                    enrichParticipant(contest, $scope.participant);
                }
                return $scope.participant;
            };

            function enrichParticipant(contest, participant) {
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

            /////////////////////////////////////////////////////////////////////
            //          Order Functions
            /////////////////////////////////////////////////////////////////////

            $scope.getActiveOrders = function (contest) {
                var participant = $scope.getParticipant(contest);
                return participant ? (participant.orders || []) : [];
            };

            $scope.getClosedOrders = function (contest) {
                var participant = $scope.getParticipant(contest);
                return participant ? (participant.orderHistory || []) : [];
            };

            $scope.popupNewOrderDialog = function () {
                NewOrderDialog.popup({symbol: QuoteService.lastSymbol});
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

            $scope.isClosedOrderSelected = function () {
                return $scope.selectedClosedOrder != null;
            };

            $scope.selectClosedOrder = function (order) {
                $scope.selectedClosedOrder = order;
            };

            $scope.toggleSelectedClosedOrder = function () {
                $scope.selectedClosedOrder = null;
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
                var participant = $scope.getParticipant(contest);
                return participant ? (participant.fundsAvailable || 0) : 0;
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
                $scope.participant = null;
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
             * Watch for contest change events
             */
            $scope.$watch(MySession.contest, function () {
                reset();
            }, true);

        }]);

})();