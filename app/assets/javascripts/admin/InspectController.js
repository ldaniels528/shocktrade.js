(function () {
    var app = angular.module('shocktrade');

    /**
     * Inspect Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('InspectController', ['$scope', '$http', '$interval', '$log', '$routeParams', '$timeout', 'toaster', 'ContestService', 'MySession',
        function ($scope, $http, $interval, $log, $routeParams, $timeout, toaster, ContestService, MySession) {

            $scope.expandItem = function (item) {
                item.expanded = !item.expanded;
            };

            $scope.expandPlayer = function (player) {
                player.expanded = !player.expanded;
                if(!player.myOpenOrders) player.myOpenOrders = {};
                if(!player.myClosedOrders) player.myClosedOrders = {};
                if(!player.myPositions) player.myPositions = {};
                if(!player.myPerformance) player.myPerformance = {};
            };

            (function () {
                if ($routeParams.contestId) {
                    var contestId = $routeParams.contestId;
                    $log.info("Attempting to load contest " + contestId);

                    // load the contest
                    ContestService.getContestByID(contestId)
                        .success(function(contest) {
                            MySession.setContest(contest);
                        })
                        .error(function(err) {
                            toaster.pop('error', 'Failed to load contest ' + contestId, null);
                        });
                }
            })();

        }]);

})();