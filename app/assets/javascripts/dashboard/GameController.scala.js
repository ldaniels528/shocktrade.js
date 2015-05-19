(function () {
    var app = angular.module('shocktrade');

    /**
     * Game Play Controller
     */
    app.controller('GameController', ['$scope', '$location', '$log', '$routeParams', '$timeout', 'toaster', 'MySession', 'ContestService', 'QuoteService',
        function ($scope, $location, $log, $routeParams, $timeout, toaster, MySession, ContestService, QuoteService) {

            // setup the public variables
            $scope.contest = null;
            $scope.myContests = null;
            $scope.searchResults = [];
            $scope.searchTerm = "";
            $scope.selectedContest = null;
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

            // private variables
            var totalInvestment = 0.0;

            ///////////////////////////////////////////////////////////////////////////
            //          Functions
            ///////////////////////////////////////////////////////////////////////////

            $scope.enterGame = function (contest) {
                $scope.contest = contest;
                MySession.setContest(contest);
                $location.path("/dashboard/" + contest.OID());
                //$scope.changePlayTab(1);
                // TODO switch back if not working
            };

            $scope.getAvailableCount = function () {
                var count = 0;
                angular.forEach($scope.searchResults, function (r) {
                    if (r.status === 'ACTIVE') count++;
                });
                return count;
            };

            $scope.getAvailableSlots = function (contest, row) { // 0,1
                var slots = [];
                if (contest) {
                    var participants = contest.participants || [];
                    var start = row * 8; // 0=0, 1=8, 2=16
                    var end = row * 8 + 8; // 0=7, 1=8, 2=15
                    for (var n = start; n < end; n++) {
                        slots.push((n < participants.length) ? participants[n] : null);
                    }
                }
                return slots;
            };

            $scope.getTotalInvestment = function (playerId) {
                // lookup the player
                var player = ContestService.findPlayerByID(contest, playerId);
                if (player && !player.totalInvestment && !player.loadingTotalInvestment) {
                    player.loadingTotalInvestment = true;

                    // set a timeout so that loading doesn't persist
                    $timeout(function () {
                        player.totalInvestment = 0.00;
                    }, 10000);

                    ContestService.getTotalInvestment(playerId)
                        .success(function (totalInvestment) {
                            player.totalInvestment = totalInvestment;
                            player.loadingTotalInvestment = false;
                        })
                        .error(function (response) {
                            toaster.pop('error', 'Error loading total investment', null);
                            player.totalInvestmentAttempts += 1;

                            // attempt to load the totalInvestment up to 3 times
                            if(player.totalInvestmentAttempts < 3) {
                                $timeout(function () {
                                    player.loadingTotalInvestment = false;
                                }, 5000);
                            }
                        });
                }

                return player ? player.totalInvestment : null;
            };

            $scope.getStatusIcon = function (c, maxPlayers) {
                if (c && c.invitationOnly) return "/assets/images/objects/locked.png";
                else {
                    var playerCount = ((c && c.participants) || []).length;
                    if (playerCount + 1 < maxPlayers) return "/assets/images/status/greenlight.png";
                    else if (playerCount + 1 === maxPlayers) return "/assets/images/status/yellowlight.gif";
                    else if (playerCount >= maxPlayers) return "/assets/images/status/redlight.png";
                    else return "/assets/images/status/offlight.png";
                }
            };

            $scope.getStatusClass = function (c, maxPlayers) {
                var playerCount = ((c && c.participants) || []).length;
                if (playerCount + 1 < maxPlayers) return "positive";
                else if (playerCount + 1 === maxPlayers) return "warning";
                else if (playerCount >= maxPlayers) return "negative";
                else return "null";
            };

            $scope.contestSearch = function (searchOptions) {
                $scope.startLoading();

                ContestService.findContests(searchOptions).then(
                    function (contests) {
                        $scope.searchResults = contests;
                        $scope.stopLoading();
                    },
                    function (err) {
                        toaster.pop('error', 'Error!', "Failed to execute Contest Search");
                        $scope.stopLoading();
                    });
            };

            $scope.getMyContests = function () {
                var userID = MySession.getUserID();
                if (userID == null) return [];
                else if ($scope.myContests === null || $scope.myContests === undefined) {
                    $scope.myContests = [];
                    $log.info("getMyContests: contests need loading - userID = " + userID);
                    ContestService.getContestsByPlayerID(userID)
                        .success(function (contests) {
                            $log.info("Loaded " + contests.length + " contest(s)");
                            $scope.myContests = contests;
                        })
                        .error(function (response) {
                            $log.error("Failed to load 'My Contests'");
                            $timeout(function () {
                                $scope.myContests = null;
                            }, 15000);
                        });
                }

                return $scope.myContests;
            };

            $scope.getSearchResults = function (searchTerm) {
                var term = (searchTerm || "").trim().toLowerCase();
                if (term.length === 0) return $scope.searchResults;
                else {
                    // $log.info("searchTerm = " + searchTerm);
                    return $scope.searchResults.filter(function (c) {
                        return c.name.toLowerCase().indexOf(term) != -1;
                    });
                }
            };

            $scope.loadContest = function (contestId) {
                // load the contest
                ContestService.getContestByID(contestId)
                    .success(function (contest) {
                        $scope.switchToContest(contest);
                    })
                    .error(function (xhr, status, error) {
                        $log.error("Error selecting feed: " + xhr.error);
                        toaster.pop('error', 'Error!', "Error loading game");
                    });
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

            $scope.isContestOwner = function (contest) {
                return contest && contest.creator.name === MySession.getUserName();
            };

            $scope.isDeletable = function (contest) {
                return $scope.isContestOwner(contest) && (!contest.startTime || contest.participants.length == 1);
            };

            $scope.isParticipant = function (contest) {
                var id = MySession.getUserID();
                return id && ContestService.findPlayerByID(contest, id) != null;
            };

            $scope.deleteContest = function (contest) {
                contest.deleting = true;
                ContestService.deleteContest(contest.OID())
                    .success(function (response) {
                        removeContestFromList(contest.OID());
                        $timeout(function () {
                            contest.deleting = false;
                        }, 500);
                    })
                    .error(function (err) {
                        toaster.pop('error', 'Error!', "Failed to delete contest");
                        $log.error("An error occurred while deleting the contest");
                        $timeout(function () {
                            contest.deleting = false;
                        }, 500);
                    });
            };

            $scope.isJoinable = function (contest) {
                return contest && !contest.invitationOnly && !$scope.isContestOwner(contest) && !$scope.isParticipant(contest);
            };

            $scope.joinContest = function (contest) {
                contest.joining = true;
                ContestService.joinContest(contest.OID(), {
                    player: {
                        "id": MySession.getUserID(),
                        "name": MySession.getUserName(),
                        "facebookID": MySession.fbUserID
                    }
                }).success(function (contest) {
                    if (!contest) {
                        toaster.pop('error', 'Error!', "Failed to join game");
                        $log.error("Returned contest was null")
                    }
                    else if (contest.error) {
                        toaster.pop('error', 'Error!', contest.error);
                        $log.error(contest.error);
                    }
                    else {
                        $scope.contest = contest;
                        MySession.setContest(contest);
                        //MySession.deduct(contest.startingBalance);
                        updateWithRankings(MySession.getUserName(), contest)
                    }

                    $timeout(function () {
                        contest.joining = false;
                    }, 500);

                }).error(function (err) {
                    toaster.pop('error', 'Error!', "Failed to join contest");
                    $log.error("An error occurred while joining the contest");
                    $timeout(function () {
                        contest.joining = false;
                    }, 500);
                });
            };

            $scope.joinedParticipant = function (contest, userProfile) {
                return $scope.containsPlayer(contest, userProfile);
            };

            $scope.quitContest = function (contest) {
                contest.quitting = true;
                ContestService.quitContest(contest.OID(), MySession.getUserID())
                    .success(function (updatedContest) {
                        if (!updatedContest.error) {
                            $scope.contest = updatedContest;
                            MySession.setContest(updatedContest);
                        }
                        else {
                            $log.error("error = " + updatedContest.error);
                            toaster.pop('error', 'Error!', "Unable to process your quit command at this time.")
                        }

                        $timeout(function () {
                            contest.quitting = false;
                        }, 500);
                    })
                    .error(function (err) {
                        $log.error("An error occurred while joining the contest");
                        $timeout(function () {
                            contest.quitting = false;
                        }, 500);
                    });
            };

            $scope.startContest = function (contest) {
                contest.starting = true;
                ContestService.startContest(contest.OID())
                    .success(function (contest) {
                        if (contest.error) {
                            toaster.pop('error', 'Error!', contest.error);
                            $log.error(contest.error);
                        }

                        $timeout(function () {
                            contest.starting = false;
                        }, 500);
                    })
                    .error(function (err) {
                        $log.error("An error occurred while starting the contest");
                        $timeout(function () {
                            contest.starting = false;
                        }, 500);
                    });
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

            $scope.containsPlayer = function (contest, userProfile) {
                return userProfile.id && ContestService.findPlayerByID(contest, userProfile.OID()) != null;
            };

            $scope.contestStatusClass = function (contest) {
                if (contest == null) return "";
                else if (contest.status == 'ACTIVE') return "positive";
                else if (contest.status == 'CLOSED') return "negative";
                else return "";
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

            $scope.toggleSplitScreen = function () {
                $scope.splitScreen = false;
            };

            $scope.isSplitScreen = function () {
                return $scope.splitScreen && ($scope.selectedContest != null);
            };

            $scope.selectContest = function (contest) {
                $log.info("Selecting contest '" + contest.name + "' (" + contest.OID() + ")");
                $scope.selectedContest = contest;
                $scope.splitScreen = true;

                enrichContest(contest);
            };

            $scope.switchToContest = function (contest) {
                $scope.contest = contest;
                MySession.setContest(contest);

                // cache the player's name
                var playerName = MySession.getUserName();

                // find participant that represents the player
                var participant = ContestService.findPlayerByID(contest, MySession.getUserID());

                // load the enriched participant
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

            /////////////////////////////////////////////////////////////////////
            //          Positions and Orders
            /////////////////////////////////////////////////////////////////////

            $scope.cancelOrder = function (contestId, playerId, orderId) {
                ContestService.deleteOrder(contestId, playerId, orderId)
                    .success(function (participant) {
                        updateWithPricing(participant);
                    })
                    .error(function (err) {
                        toaster.pop('error', 'Error!', "Failed to cancel order");
                    });
            };

            //////////////////////////////////////////////////////////////////////
            //              Private Methods
            //////////////////////////////////////////////////////////////////////

            function enrichContest(contest) {
                if (!contest.rankings) {
                    updateWithRankings(MySession.getUserName(), contest);
                }
            }

            function isContestSelected(contestId) {
                return ($scope.contest && $scope.contest.OID() === contestId);
            }

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

            function indexOfContest(contestId) {
                for (var n = 0; n < $scope.searchResults.length; n++) {
                    var contest = $scope.searchResults[n];
                    if (contest.OID() === contestId) return n;
                }
                return -1;
            }

            function removeContestFromList(contestId) {
                var index = indexOfContest(contestId);
                if (index != -1) {
                    $scope.searchResults.splice(index, 1);
                }

                // if the delete contest is selected, change the selection
                if (isContestSelected(contestId)) {
                    $scope.contest = null;
                    MySession.setContest(null);
                    $scope.splitScreen = false;
                }
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

            /**
             * Updates the contest with participant rankings
             */
            function updateWithRankings(playerName, contest) {
                ContestService.getRankings(contest.OID()).then(
                    function (response) {
                        contest.rankings = response.data;
                        if (contest.rankings.length) {
                            contest.leader = contest.rankings[0];
                        }

                        // capture the rankings for the current player
                        var participant = findPlayerByName(contest, playerName);
                        if (participant) {
                            for (var n = 0; n < contest.rankings.length; n++) {
                                var ranking = contest.rankings[n];
                                if (ranking.name === playerName) {
                                    participant.gainLoss = ranking.gainLoss;
                                    participant.rank = ranking.rank;
                                }
                            }
                        }
                    },
                    function (err) {
                        $log.error("An error occurred loading rankings")
                    });
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

            //////////////////////////////////////////////////////////////////////
            //              Broadcast Event Listeners
            //////////////////////////////////////////////////////////////////////

            /**
             * Listen for contest creation events
             */
            $scope.$on("contest_created", function (event, contest) {
                $log.info("New contest created '" + contest.name + "'");
                $scope.searchResults.push(contest);
            });

            /**
             * Listen for contest deletion events
             */
            $scope.$on("contest_deleted", function (event, contest) {
                removeContestFromList(contest.OID());

                if ($scope.selectedContest && (contest.OID() === $scope.selectedContest.OID())) {
                    $scope.selectedContest = null;
                }
            });

            /**
             * Listen for contest update events
             */
            $scope.$on("contest_updated", function (event, contest) {
                $log.info("Contest '" + contest.name + "' updated");
                if (!$scope.contest || (contest.OID() === $scope.contest.OID())) {
                    var oldContest = $scope.contest;
                    $scope.contest = contest;
                    MySession.setContest(contest);
                    /*
                     if(oldContest) {
                     $scope.contest.rankings = oldContest.rankings;
                     }*/
                }
                if ($scope.selectedContest && (contest.OID() === $scope.selectedContest.OID())) {
                    $scope.selectedContest = contest;
                }
            });

            /**
             * Listen for profile update events
             */
            $scope.$on("profile_updated", function (event, profile) {
                $log.info("User Profile updated");
                if (MySession.getUserID() == profile.OID()) {
                    MySession.userProfile.netWorth = profile.netWorth;
                    //type, title, body, timeout, bodyOutputType, clickHandler
                    toaster.pop('success', 'Your Wallet', '<ul><li>Your wallet now has $' + profile.netWorth + '</li></ul>', 5000, 'trustedHtml');
                }
            });

            /**
             * Listen for player perk update events
             */
            $scope.$on("perks_updated", function (event, contestInfo) {
                if ($scope.contest && (contestInfo.OID() === $scope.contest.OID())) {
                    var player = ContestService.findPlayerByID($scope.contest, contestInfo.player.OID());
                    if (player) {
                        $log.info("Updating perks for player " + contestInfo.player.name);
                        player.perks = contestInfo.perks;
                    }
                }
            });

            //////////////////////////////////////////////////////////////////////
            //              Watch Event Listeners
            //////////////////////////////////////////////////////////////////////

            // watch for changes to the player's profile
            $scope.$watch("MySession.userProfile", function (newVal) {
                var playerName = MySession.getUserName();
                if (playerName !== 'Spectator') {
                    // load the player's games
                    $scope.loadContestsByPlayerID(MySession.getUserID());
                }
            });

            ///////////////////////////////////////////////////////////////////////////
            //          Initialization
            ///////////////////////////////////////////////////////////////////////////

            // perform the initial search
            $scope.contestSearch($scope.searchOptions);

        }]);
})();