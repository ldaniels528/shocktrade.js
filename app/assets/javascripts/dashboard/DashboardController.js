(function () {
    var app = angular.module('shocktrade');

    /**
     * Dashboard Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('DashboardController', ['$scope', '$location', '$log', '$routeParams', '$timeout', 'toaster', 'MySession', 'ContestService',
        function ($scope, $location, $log, $routeParams, $timeout, toaster, MySession, ContestService) {

            ///////////////////////////////////////////////////////////////////////////
            //          Initialization
            ///////////////////////////////////////////////////////////////////////////

            (function () {
                // was the contest ID passed?
                var contestId = $routeParams.contestId;
                if (contestId) {
                    $log.info("Looking for Contest # " + contestId);
                    if ($scope.searchResults && $scope.searchResults.length) {
                        angular.toJson($scope.searchResults, function (c) {
                            if (c.OID() === contestId) {
                                $scope.switchToContest(c);
                            }
                        });
                    }
                    else {
                        $scope.loadContest(contestId);
                    }
                }
            })();

        }]);

})();