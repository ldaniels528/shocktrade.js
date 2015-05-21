(function () {
    var app = angular.module('shocktrade');

    /**
     * Game Play Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('GameController', ['$scope', '$location', '$log', '$routeParams', '$timeout', 'toaster', 'MySession', 'ContestService', 'QuoteService',
        function ($scope, $location, $log, $routeParams, $timeout, toaster, MySession, ContestService, QuoteService) {

            // setup the public variables
            $scope.contest = null;

            ///////////////////////////////////////////////////////////////////////////
            //          Functions
            ///////////////////////////////////////////////////////////////////////////

            $scope.enterGame = function (contest) {
                MySession.setContest(contest);
                $scope.contest = contest;

                $location.path("/dashboard/" + contest.OID());
                $scope.tabIndex = 1;
                //$scope.changePlayTab(1);
                // TODO switch back if not working
            };

            $scope.loadContestsByPlayerID = function (playerId) {
                if (playerId) {
                    $scope.startLoading();
                    ContestService.getContestsByPlayerID(playerId)
                        .success(function (contests) {
                            $scope.myContests = contests;
                            $scope.stopLoading();
                        })
                        .error(function (response) {
                            $log.error("Failed to load My Contests - " + response.error);
                            $scope.stopLoading();
                        });
                }
            };

            $scope.changePlayTab = function (tabIndex) {
                var tab = $scope.playTabs[tabIndex];
                $log.info("Changing location to " + tab.url);
                $location.path(tab.url);
                $scope.tabIndex = tabIndex;
                return true;
            };

            $scope.setPlayActiveTab = function (tabIndex) {
                $log.info("Setting Play active tab to #" + tabIndex + " (" + $scope.playTabs[tabIndex].url + ")");

                // make all of the tabs inactive
                angular.forEach($scope.playTabs, function (tab) {
                    tab.active = false;
                });
                $scope.playTabs[tabIndex].active = true;
            };

            $scope.changeArrow = function (change) {
                return change < 0 ? "stock_down.gif" : ( change > 0 ? "stock_up.gif" : "transparent.png" );
            };

            $scope.changeContest = function (contest) {
                $log.info("Changing contest to " + contest.OID());
                $scope.contest = contest;
                MySession.contest = contest;
            };

            /**
             * Returns the contacts matching the given search term
             */
            $scope.getRegisteredFriends = function () {
                var fbFriends = MySession.fbFriends;
                /*
                 return fbFriends.filter(function(friend) {
                 return friend.profile;
                 });*/
                return fbFriends.slice(0, 20);
            };

            $scope.switchToContest = function (contest) {
                $scope.contest = contest;
                MySession.setContest(contest);

                // find participant that represents the player
                var participant = ContestService.findPlayerByID(contest, MySession.getUserID());

                // load the enriched participant
                var playerName = MySession.getUserName();
                if (!contest.rankings) {
                    updateWithRankings(playerName, contest);
                }

                // load the pricing for the participant's position
                if (participant) {
                    updateWithPricing(participant);
                }
            };

            $scope.isMedalist = function (rank) {
                return rank === '1st' || rank === '2nd' || rank === '3rd';
            };

            $scope.gainLoss = function (tx) {
                var cost = (tx.pricePaid || 0) * tx.quantity;
                var gross = (tx.priceSold || 0) * tx.quantity;
                var commissions = (tx.commision1 || 0) + (tx.commision2 || 0) + (tx.commission || 0);
                var net = gross - (cost + commissions);
                return net != 0 ? ( net / cost) * 100 : 0;
            };

            $scope.trophy = function (place) {
                switch (place) {
                    case '1st':
                        return "contests/gold.png";
                    case '2nd':
                        return "contests/silver.png";
                    case '3rd':
                        return "contests/bronze.png";
                    default:
                        return "status/transparent.png";
                }
            };

            //////////////////////////////////////////////////////////////////////
            //              Private Methods
            //////////////////////////////////////////////////////////////////////

            function placeName(n) {
                switch (n) {
                    case 1:
                    case 21:
                        return n + "st";
                    case 2:
                    case 22:
                        return n + "nd";
                    case 3:
                    case 23:
                        return n + "rd";
                    default:
                        return n + "th";
                }
            }

            function loadLeaderRankings(contests) {
                angular.forEach(contests, function (contest) {
                    ContestService.getRankings(contest.OID()).then(function (response) {
                        $log.info("Loading rankings for contest " + contest.OID() + "...");
                        var rankings = response.data;
                        contest.rankings = rankings;
                        if (rankings.length) {
                            contest.leader = rankings[0];
                        }
                    });
                });
            }

            function loadPlayerRankings(contests, playerName) {
                angular.forEach(contests, function (contest) {
                    // is the player in the contest?
                    if ($scope.containsPlayer(contest.participants, playerName)) {
                        $log.info("Loading rankings for contest " + contest.OID() + " for player " + playerName + "...");
                        ContestService.getRankings(contest.OID()).then(function (response) {
                            var rankings = response.data;
                            contest.rankings = rankings;
                            contest.leader = findPlayerByName(rankings, playerName);
                        });
                    }
                });
            }

            /**
             * Updates a participant with pricing information
             */
            function updateWithPricing(participant) {
                // retrieve the symbols
                var symbols = (participant.positions || []).map(function (p) {
                    return p.symbol;
                });

                // load the symbols
                if (symbols.length) {
                    QuoteService.getPricing(symbols).then(function (response) {
                        var mapping = response.data;
                        for (var n = 0; n < participant.positions.length; n++) {
                            var pos = participant.positions[n];
                            var value = mapping[pos.symbol];
                            if (value != null) {
                                pos.lastTrade = value.lastTrade;
                                pos.netValue = pos.quantity * value.lastTrade;
                                pos.gainLoss = 100.0 * ((pos.netValue - pos.cost) / pos.cost);
                            }
                        }
                    });
                }
            }

            //////////////////////////////////////////////////////////////////////
            //              Data Graphs
            //////////////////////////////////////////////////////////////////////

            // define the levels
            $scope.levels = [
                {"number": 1, "nextLevelXP": 1000, "description": "Private"},
                {"number": 2, "nextLevelXP": 2000, "description": "Private 1st Class"},
                {"number": 3, "nextLevelXP": 4000, "description": "Corporal"},
                {"number": 4, "nextLevelXP": 8000, "description": "First Corporal"},
                {"number": 5, "nextLevelXP": 16000, "description": "Sergeant"},
                {"number": 6, "nextLevelXP": 32000, "description": "Staff Sergeant"},
                {"number": 7, "nextLevelXP": 64000, "description": "Gunnery Sergeant"},
                {"number": 8, "nextLevelXP": 1280000, "description": "Master Sergeant"},
                {"number": 9, "nextLevelXP": 256000, "description": "First Sergeant"},
                {"number": 10, "nextLevelXP": 1024000, "description": "Sergeant Major"},
                {"number": 11, "nextLevelXP": 2048000, "description": "Warrant Officer 3rd Class"},
                {"number": 12, "nextLevelXP": 4096000, "description": "Warrant Officer 2nd Class"},
                {"number": 13, "nextLevelXP": 4096000, "description": "Warrant Officer 1st Class"},
                {"number": 14, "nextLevelXP": 8192000, "description": "Chief Warrant Officer"},
                {"number": 15, "nextLevelXP": 8192000, "description": "Master Chief Warrant Officer"},
                {"number": 16, "nextLevelXP": 16384000, "description": "Lieutenant"},
                {"number": 17, "nextLevelXP": 32768000, "description": "First Lieutenant"},
                {"number": 18, "nextLevelXP": 65536000, "description": "Captain"},
                {"number": 19, "nextLevelXP": 131072000, "description": "Major"},
                {"number": 20, "nextLevelXP": 262144000, "description": "Lieutenant Colonel"},
                {"number": 21, "nextLevelXP": 524288000, "description": "Colonel"},
                {"number": 22, "nextLevelXP": 524288000, "description": "Brigadier General"},
                {"number": 23, "nextLevelXP": 524288000, "description": "Major General"},
                {"number": 24, "nextLevelXP": 524288000, "description": "Lieutenant General"},
                {"number": 25, "nextLevelXP": 524288000, "description": "General"}];

        }]);
})();