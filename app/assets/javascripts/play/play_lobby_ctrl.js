/**
 * Play Lobby Controller
 * @author lawrence.daniels@gmail.com
 */
angular
    .module('shocktrade')
    .controller('PlayLobbyCtrl', ['$scope', '$location', '$log', '$timeout', 'MySession', 'ContestService', 'Errors', 'InvitePlayerDialog', 'QuoteService',
        function ($scope, $location, $log, $timeout, MySession, ContestService, Errors, InvitePlayerDialog, QuoteService) {

            // progress variables
            $scope.joining = false;
            $scope.quiting = false;

            $scope.joinedParticipant = function (contest, userProfile) {
                return $scope.containsPlayer(contest, userProfile);
            };

            $scope.deleteGame = function (contest) {
                $scope.quitingGame = true;
                var contestId = contest._id.$oid;
                $log.info("Deleting game # " + contestId + "...");
                ContestService.deleteContest(contestId)
                    .success(function (response) {
                        $log.info("deleteContest: response = " + angular.toJson(response));
                        $timeout(function () {
                            $scope.quitingGame = false;
                        }, 500);
                    })
                    .error(function (err) {
                        Errors.addMessage("Failed to delete contest");
                        $timeout(function () {
                            $scope.quitingGame = false;
                        }, 500);
                    });
            };

            $scope.joinGame = function () {
                $scope.joiningGame = true;
                var contestId = MySession.contestId;
                var playerId = MySession.userProfile.id;
                var playerName = MySession.userProfile.name;
                var facebookID = MySession.fbUserID;
                var playerInfo = {"player": {"id": playerId, "name": playerName, "facebookID": facebookID}};
                ContestService.joinContest(contestId, playerInfo).then(
                    function (contest) {
                        if (!contest.error)
                            $log.info("join game succeeded");
                        else
                            $log.error("join game failed");

                        $timeout(function () {
                            $scope.joiningGame = false;
                        }, 500);
                    },
                    function () {
                        $log.error("join game failed");
                        $timeout(function () {
                            $scope.joiningGame = false;
                        }, 500);
                    });
            };

            $scope.quitGame = function () {
                $scope.quitingGame = true;
                var contestId = MySession.contestId;
                var playerId = MySession.userProfile.id;
                $log.info("Quiting game # " + contestId + "...");
                ContestService.quitContest(contestId, playerId)
                    .success(function () {
                        $log.info("quit game succeeded");
                        $timeout(function () {
                            $scope.quitingGame = false;
                        }, 500);
                    })
                    .error(function () {
                        $log.error("quit game failed");
                        $timeout(function () {
                            $scope.quitingGame = false;
                        }, 500);
                    });
                return false;
            };

        }]);