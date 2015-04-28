angular
    .module('shocktrade')
    .controller('PlaySearchCtrl', ['$scope', '$location', '$log', '$timeout', 'MySession', 'ContestService', 'Errors', 'NewGameDialog',
        function ($scope, $location, $log, $timeout, MySession, ContestService, Errors, NewGameDialog) {

            // setup contest variables
            $scope.myContests = [];
            $scope.searchMyGame = "";
            $scope.participant = {};

            // search variables
            $scope.searchOptions = {
                activeOnly: true,
                available: false,
                perksAllowed: false,
                levelCap: "1",
                levelCapAllowed: false
            };
            $scope.searchResults = [];

            $scope.popupNewGameDialog = function () {
                NewGameDialog.popup($scope);
            };

            $scope.getAvailableCount = function () {
                var count = 0;
                $scope.searchResults.forEach(function (r) {
                    if (r.status === 'ACTIVE') count++;
                });
                return count;
            };

            $scope.contestSearch = function (searchOptions) {
                $scope.startLoading();
                $scope.message = "";

                ContestService.findContests(searchOptions).then(
                    function (contests) {
                        $scope.searchResults = contests;
                        if (contests.length) {
                            // make sure the contest ID is set
                            var contestId = contests[0]._id.$oid;
                            if (( !MySession.contestId || MySession.contestId === "" ) && (MySession.contestId != contestId)) {
                                $scope.selectContest(contests[0]);
                            }

                            // load the rankings for the current player or leader
                            var playerName = MySession.userProfile.name;
                            if (playerName != 'Spectator') {
                                loadPlayerRankings(contests, playerName);
                            }
                            else {
                                loadLeaderRankings(contests);
                            }
                        }
                        $scope.stopLoading();
                    },
                    function (err) {
                        $scope.message = "Failed to execute Contest Search";
                        $scope.stopLoading();
                    });
            };

            $scope.getMyContests = function (searchTerm) {
                var term = searchTerm.toLowerCase();
                if (!term || term === "") return $scope.myContests;
                else {
                    return $scope.myContests.filter(function (c) {
                        return c.name.toLowerCase().indexOf(term) != -1;
                    });
                }
            };

            $scope.loadContestsByPlayerID = function (playerId) {
                if (playerId) {
                    $scope.startLoading();
                    $scope.message = "";
                    ContestService.getContestsByPlayerID(playerId)
                        .then(function (contests) {
                            $scope.myContests = contests;
                            if (contests.length > 0 && MySession.contestId == null) {
                                MySession.contestId = contests[0]._id.$oid;
                            }
                            $scope.stopLoading();
                        },
                        function (err) {
                            $scope.message = "Failed to load My Contests";
                            $scope.stopLoading();
                        });
                }
            };

            $scope.loadRestrictionTypes = function () {
                ContestService.getRestrictions()
                    .success(function (restrictions) {
                        $scope.restrictions = restrictions;
                    })
                    .error(function (err) {
                        $log.error("An error occurred loading the access restrictions");
                    });
            };

            //////////////////////////////////////////////////////////////////////
            //              Private Methods
            //////////////////////////////////////////////////////////////////////

            function findPlayerByName(contest, playerName) {
                var participants = contest.participants;
                for (var n = 0; n < participants.length; n++) {
                    var participant = participants[n];
                    if (participant.name === playerName) {
                        return participant;
                    }
                }
                return null;
            }

            function loadPlayerRankings(contests, playerName) {
                for (var n = 0; n < contests.length; n++) {
                    var contest = contests[n];

                    // is the player in the contest?
                    if ($scope.containsPlayer(contest.participants, playerName)) {
                        ContestService.getRankingsWithCallback(contest, function (contest) {
                            contest.leader = findPlayerByName(contest.rankings, playerName);
                        });
                    }
                }
            }

            function loadLeaderRankings(contests) {
                for (var n = 0; n < contests.length; n++) {
                    var contest = contests[n];
                    ContestService.getRankingsWithCallback(contest, function (contest) {
                        contest.leader = contest.rankings[0];
                    });
                }
            }

            function indexOfContest(contestId) {
                for (var n = 0; n < $scope.searchResults.length; n++) {
                    var contest = $scope.searchResults[n];
                    if (contest.id == contestId) return n;
                }
                return -1;
            }

            function removeContestFromList(contestId) {
                var index = indexOfContest(contestId);
                if (index != -1) {
                    $scope.searchResults.splice(index, 1);
                }
            }

            //////////////////////////////////////////////////////////////////////
            //              Event Listeners
            //////////////////////////////////////////////////////////////////////

            /**
             * Listen for contest creation events
             */
            $scope.$on("contest_created", function (event, contest) {
                $log.info("New contest created '" + contest.name + "'");
                insertID(contest);
                $scope.searchResults.push(contest);
            });

            /**
             * Listen for contest deletion events
             */
            $scope.$on("contest_deleted", function (event, contest) {
                $log.info("Play/Search: Contest '" + contest.id + "' deleted");
                removeContestFromList(contest.id);
            });

            /**
             * Listen for contest update events
             */
            $scope.$on("contest_updated", function (event, contest) {
                $log.info("Contest '" + contest.name + "' updated");
                insertID(contest);
            });

            $scope.$watch("searchOptions", function () {
                $scope.contestSearch($scope.searchOptions);
            }, true);

            // watch for changes to the player's profile
            $scope.$watch("MySession.userProfile", function (oldVal, newVal) {
                var playerName = MySession.userProfile.name;
                if (playerName !== 'Spectator') {
                    // load the player's games
                    $scope.loadContestsByPlayerID(MySession.userProfile.id);

                    // load the contest
                    $scope.loadContest(MySession.contestId);
                }
            });

            // load the restriction types
            $scope.loadRestrictionTypes();

        }]);
