(function () {
    var app = angular.module('shocktrade');

    /**
     * Inspect Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('InspectController', ['$scope', '$http', '$interval', '$log', '$routeParams', '$timeout', 'toaster', 'ContestService', 'MySession',
        function ($scope, $http, $interval, $log, $routeParams, $timeout, toaster, ContestService, MySession) {

            $scope.contest = null;

            $scope.expandItem = function (item) {
                item.expanded = !item.expanded;
            };

            $scope.expandPlayer = function (player) {
                player.expanded = !player.expanded;
                if (!player.myOpenOrders) player.myOpenOrders = {};
                if (!player.myClosedOrders) player.myClosedOrders = {};
                if (!player.myPositions) player.myPositions = {};
                if (!player.myPerformance) player.myPerformance = {};
            };

            $scope.getOpenOrders = function (contest) {
                var orders = [];
                if(contest) {
                    angular.forEach(contest.participants, function (participant) {
                        angular.forEach(participant.orders, function (order) {
                            order.owner = participant;
                            orders.push(order);
                        });
                    });
                }
                return orders;
            };

            $scope.updateContestHost = function (host) {
                $http.post("/api/contest/" + $scope.contest.OID() + "/host", {"host": host})
                    .success(function (response) {
                        toaster.pop('success', 'Processing host updated', null);
                    })
                    .error(function (err) {
                        toaster.pop('error', 'Failed to update processing host', null);
                    });
            };

            (function () {
                if ($routeParams.contestId) {
                    var contestId = $routeParams.contestId;
                    $log.info("Attempting to load contest " + contestId);

                    // load the contest
                    ContestService.getContestByID(contestId)
                        .success(function (contest) {
                            $scope.contest = contest;
                            MySession.setContest(contest);
                        })
                        .error(function (err) {
                            toaster.pop('error', 'Failed to load contest ' + contestId, null);
                        });
                }
            })();

        }]);

})();