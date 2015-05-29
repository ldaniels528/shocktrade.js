(function () {
    var app = angular.module('shocktrade');

    /**
     * Dashboard Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('DashboardController', ['$scope', '$log', '$routeParams', '$timeout', 'toaster', 'ContestService', 'TransferFundsDialog', 'MySession', 'PerksDialog',
        function ($scope, $log, $routeParams, $timeout, toaster, ContestService, TransferFundsDialog, MySession, PerksDialog) {

            var accountMode = false;

            $scope.isCashAccount = function () {
                return !accountMode;
            };

            $scope.isMarginAccount = function () {
                return accountMode;
            };

            $scope.toggleAccountMode = function () {
                accountMode = !accountMode;
            };

            $scope.getAccountMode = function () {
                return accountMode;
            };

            $scope.getAccountType = function () {
                return $scope.isMarginAccount() ? "MARGIN" : "CASH";
            };

            /////////////////////////////////////////////////////////////////////
            //          Pop-up Dialog Functions
            /////////////////////////////////////////////////////////////////////

            $scope.marginAccountDialog = function () {
                TransferFundsDialog.popup({
                    "success": function (contest) {
                        MySession.setContest(contest);
                    }
                });
            };

            $scope.perksDialog = function () {
                PerksDialog.popup({});
            };

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

            ///////////////////////////////////////////////////////////////////////////
            //          Initialization
            ///////////////////////////////////////////////////////////////////////////

            (function () {
                // was the contest ID passed?
                var contestId = $routeParams.contestId;
                if (contestId) {
                    // if the current contest is not the chosen contest ...
                    if (MySession.getContestID() !== contestId) {
                        $log.info("Loading contest " + contestId + "...");

                        ContestService.getContestByID(contestId)
                            .success(function (contest) {
                                MySession.setContest(contest);
                            })
                            .error(function (xhr, status, error) {
                                $log.error("Error selecting feed: " + xhr.error);
                                toaster.pop('error', 'Error!', "Error loading game");
                            });
                    }
                }
            })();

        }]);

})();