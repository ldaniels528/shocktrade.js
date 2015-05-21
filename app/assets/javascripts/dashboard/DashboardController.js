(function () {
    var app = angular.module('shocktrade');

    /**
     * Dashboard Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('DashboardController', ['$scope', '$location', '$log', '$routeParams', '$timeout', 'toaster', 'MySession', 'ContestService',
        function ($scope, $location, $log, $routeParams, $timeout, toaster, MySession, ContestService) {

            var loadingContest = false;

            ///////////////////////////////////////////////////////////////////////////
            //          Initialization
            ///////////////////////////////////////////////////////////////////////////

            (function () {
                // was the contest ID passed?
                var contestId = $routeParams.contestId;
                if (contestId) {
                    // if the current contest is not the chosen contest ...
                    if (MySession.getContestID() !== contestId && !loadingContest) {
                        loadingContest = true;
                        $log.info("Loading contest " + contestId + "...");

                        ContestService.getContestByID(contestId)
                            .success(function (contest) {
                                MySession.setContest(contest);
                                loadingContest = false;
                            })
                            .error(function (xhr, status, error) {
                                loadingContest = false;
                                $log.error("Error selecting feed: " + xhr.error);
                                toaster.pop('error', 'Error!', "Error loading game");
                            });
                    }
                }
            })();

        }]);

})();