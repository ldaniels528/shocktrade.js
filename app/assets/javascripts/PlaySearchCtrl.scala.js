angular
    .module('shocktrade')
    .controller('PlaySearchCtrl', ['$scope', '$location', '$log', '$timeout', 'MySession', 'ContestService', 'Errors', 'NewGameDialog',
        function ($scope, $location, $log, $timeout, MySession, ContestService, Errors, NewGameDialog) {

            // setup contest variables
            $scope.myContests = [];
            $scope.searchTerm = null;
            $scope.participant = {};
            $scope.splitScreen = false;

            // search variables
            $scope.searchOptions = {
                activeOnly: true,
                available: true,
                friendsOnly: false,
                levelCap: "1",
                levelCapAllowed: false,
                perksAllowed: false,
                robotsAllowed: false
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

            $scope.getStatusIcon = function (c) {
                var playerCount = ((c && c.participants) || []).length;
                if (playerCount + 1 < $scope.maxPlayers) return "/assets/images/status/greenlight.png";
                else if (playerCount + 1 === $scope.maxPlayers) return "/assets/images/status/yellowlight.gif";
                else if (playerCount >= $scope.maxPlayers) return "/assets/images/status/redlight.png";
                else return "/assets/images/status/offlight.png";
            };

            $scope.getStatusClass = function (c) {
                var playerCount = ((c && c.participants) || []).length;
                if (playerCount + 1 < $scope.maxPlayers) return "positive";
                else if (playerCount + 1 === $scope.maxPlayers) return "warning";
                else if (playerCount >= $scope.maxPlayers) return "negative";
                else return "null";
            };

            $scope.contestSearch = function (searchOptions) {
                $scope.startLoading();
                $scope.message = "";

                ContestService.findContests(searchOptions).then(
                    function (contests) {
                        $scope.searchResults = contests;
                        if (contests.length) {
                            $log.info("Preparing to load rankings for " + contests.length + " contest(s)");

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

            $scope.getMyContests = function () {
                var term = $scope.searchTerm;
                if (!term || term.trim() === "") return $scope.myContests;
                else {
                    term = term.toLowerCase();
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

            $scope.isContestOwner = function(contest) {
                return contest && contest.creator.name === MySession.userProfile.name;
            };

            $scope.startContest = function(contest) {
                contest.starting = true;
                ContestService.startContest(contest._id.$oid)
                    .success(function (contest) {
                        if(contest.error) {
                            Errors.addMessage(contest.error);
                            $log.error(contest.error);
                        }

                        $timeout(function() {
                            contest.starting = false;
                        }, 500);
                    })
                    .error(function (err) {
                        $log.error("An error occurred while starting the contest");
                        $timeout(function() {
                            contest.starting = false;
                        }, 500);
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

            function loadEnrichedParticipant(contestId, playerId) {
                ContestService.getParticipantByID(contestId, playerId)
                    .success(function (response) {
                        // TODO $log.info("BEFORE participant = " + JSON.stringify($scope.participant, null, '\t'));
                        // TODO $log.info("AFTER participant = " + JSON.stringify(response.data, null, '\t'));
                        $scope.participant = response.data;
                    })
                    .error(function (err) {

                    });
            }

            function loadLeaderRankings(contests) {
                angular.forEach(contests, function (contest) {
                    ContestService.getRankings(contest._id.$oid).then(function(response) {
                        $log.info("Loading rankings for contest " + contest._id.$oid + "...");
                        var rankings = response.data;
                        contest.rankings = rankings;
                        if(rankings.length) {
                            contest.leader = rankings[0];
                        }
                    });
                });
            }

            function loadPlayerRankings(contests, playerName) {
                angular.forEach(contests, function (contest) {
                    // is the player in the contest?
                    if ($scope.containsPlayer(contest.participants, playerName)) {
                        $log.info("Loading rankings for contest " + contest._id.$oid + " for player " + playerName + "...");
                        ContestService.getRankings(contest._id.$oid).then(function(response) {
                            var rankings = response.data;
                            contest.rankings = rankings;
                            contest.leader = findPlayerByName(rankings, playerName);
                        });
                    }
                });
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

            $scope.$on("contest_selected", function (event, contest) {
                $log.info("Play/Search: Contest '" + contest.id + "' selected");
                $scope.splitScreen = true;

                if (!contest.rankings) {
                    $scope.updateWithRankings(MySession.userProfile.name, contest);
                    if (MySession.userProfile.id) {
                        loadEnrichedParticipant(MySession.contestId, MySession.userProfile.id);
                    }
                }
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
