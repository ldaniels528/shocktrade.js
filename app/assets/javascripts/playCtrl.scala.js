@(maxPlayers: Int)

angular
    .module('shocktrade')
    .controller('PlayCtrl', ['$rootScope', '$scope', '$location', '$log', '$timeout', 'MySession', 'ContestService', 'Errors', 'InvitePlayerDialog', 'NewGameDialog', 'NewOrderDialog', 'QuoteService',
        function ($rootScope, $scope, $location, $log, $timeout, MySession, ContestService, Errors, InvitePlayerDialog, NewGameDialog, NewOrderDialog, QuoteService) {

            // setup the current contest
            $scope.contest = null;

            // setup UI variables
            $scope.statusBar = "";
            $scope.selection = {
                exposure: "sector",
                performance: "gains"
            };

            // setup contest variables
            $scope.orderChoice = "active";

            // setup the tabs
            $scope.playTabs = [{
                "name": "Games",
                "imageURL": "/assets/images/buttons/search.png",
                "path": "/assets/views/play/search/search.htm",
                "url": "/play/search",
                "active": false,
                "lockable": false,
                "disabled": false
            }, {
                "name": "Lobby",
                "imageURL": "/assets/images/objects/reward.png",
                "path": "/assets/views/play/lobby/lobby.htm",
                "url": "/play/lobby",
                "active": false,
                "lockable": false,
                "disabled": false
            }, {
                "name": "Lounge",
                "imageURL": "/assets/images/objects/friend_header.gif",
                "path": "/assets/views/play/lounge/lounge.htm",
                "url": "/play/lounge",
                "active": false,
                "lockable": true,
                "disabled": false
            }, {
                "name": "Portfolio",
                "imageURL": "/assets/images/objects/portfolio_header.png",
                "path": "/assets/views/play/portfolio/portfolio.htm",
                "url": "/play/portfolio",
                "active": false,
                "lockable": true,
                "disabled": false
            }, {
                "name": "Awards",
                "path": "/assets/views/play/awards/awards.htm",
                "imageURL": "/assets/images/objects/award.gif",
                "url": "/play/awards",
                "active": false,
                "lockable": false,
                "disabled": false
            }, {
                "name": "Perks",
                "path": "/assets/views/play/perks/perks.htm",
                "imageURL": "/assets/images/objects/gift.png",
                "url": "/play/perks",
                "active": false,
                "lockable": false,
                "disabled": false
            }, {
                "name": "Statistics",
                "path": "/assets/views/play/statistics/statistics.htm",
                "imageURL": "/assets/images/objects/stats.gif",
                "url": "/play/statistics",
                "active": false,
                "lockable": false,
                "disabled": false
            }];

            $scope.changePlayTab = function (tabIndex) {
                console.log("Changing location to " + $scope.playTabs[tabIndex].url);
                $location.path($scope.playTabs[tabIndex].url);
                return true;
            };

            $scope.setPlayActiveTab = function (tabIndex) {
                console.log("Setting Play active tab to #" + tabIndex + " (" + $scope.playTabs[tabIndex].url + ")");

                // make all of the tabs inactive
                angular.forEach($scope.playTabs, function(tab) {
                    tab.active = false;
                });
                $scope.playTabs[tabIndex].active = true;
            };


            $scope.containsPlayer = function (contest, userProfile) {
                return userProfile.id && $scope.findPlayerByID(contest, userProfile.id) != null;
            };

            $scope.findPlayerByID = function (contest, playerId) {
                var participants = contest.participants;
                for (var n = 0; n < participants.length; n++) {
                    var participant = participants[n];
                    if (participant._id.$oid == playerId) {
                        return participant;
                    }
                }
                return null;
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
                if (contest != null) {
                    console.log("Changing contest to " + contest._id.$oid);
                    MySession.contestId = contest._id.$oid;
                }
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

            $scope.getExposureChart = function (target) {
                $scope.getChart(MySession.contestId, MySession.userProfile.name, $scope.selection.exposure, target);
            };

            $scope.getPerformanceChart = function (target) {
                $scope.getChart(MySession.contestId, MySession.userProfile.name, $scope.selection.performance, target);
            };

            $scope.getChart = function (contestId, participantName, chartName, target) {
                if ((contestId && contestId != "") && (participantName && participantName != "")) {
                    // load the chart representing the securities
                    ContestService.getChart(contestId, participantName, chartName).then(
                        function (exposureData) {
                            // create the chart title & sub-title
                            var subTitle = capitalize(chartName);
                            var title = (chartName == "gains" || chartName == "losses") ? "Performance " + subTitle : subTitle + " Exposure";

                            // construct the chart
                            var exposure = new AmCharts.AmPieChart();
                            exposure.colors = ["#00dd00", "#ff00ff", "#00ffff", "#885500", "#0044ff", "#888800"];
                            exposure.addTitle(title, 12);
                            exposure.dataProvider = exposureData;
                            exposure.titleField = "title";
                            exposure.valueField = "value";
                            exposure.sequencedAnimation = true;
                            exposure.startEffect = "elastic";
                            exposure.startDuration = 2;
                            exposure.innerRadius = "30%";
                            exposure.labelRadius = 15;

                            // 3D chart
                            exposure.depth3D = 10;
                            exposure.angle = 15;
                            exposure.write(target);
                        },
                        function (err) {
                            console.log("Error: " + angular.toJson(err));
                        });
                }
            };

            $scope.loadContest = function (contestId) {
                if (contestId && contestId != "") {
                    // load the contest
                    ContestService.getContestByID(contestId)
                        .success(function (contest) {
                            $scope.contest = contest;
                            $scope.orderChoice = contest.status == 'ACTIVE' ? "active" : "history";
                            MySession.contestId = contestId;

                            // cache the player's name
                            var playerName = MySession.userProfile.name;

                            // find participant that represents the player
                            $scope.participant = lookupParticipantByName(playerName);

                            // load the enriched participant
                            updateWithRankings(playerName, contest);

                            // load the pricing for the participant's position
                            if ($scope.participant) {
                                updateWithPricing($scope.participant);
                            }
                        })
                        .error(function (xhr, status, error) {
                            $log.error("Error selecting feed: " + error.status);
                            //toaster.pop('error', 'Error!', xhr.error);
                        });
                }
            };

            $scope.selectContest = function (contest) {
                $scope.contest = contest;
                MySession.contestId = contest._id.$oid;

                if (!contest.rankings) {
                    updateWithRankings(MySession.userProfile.name, contest);
                    if(MySession.userProfile.id) {
                        loadEnrichedParticipant(MySession.contestId, MySession.userProfile.id);
                    }
                }
            };

            $scope.starRating = function (gainLoss) {
                if (gainLoss > 250) return [1, 2, 3, 4, 5];
                else if (gainLoss > 100) return [1, 2, 3, 4];
                else if (gainLoss > 75) return [1, 2, 3];
                else if (gainLoss > 50) return [1, 2];
                else if (gainLoss > 25) return [1];
                else return [];
            };

            $scope.isMedalist = function (rank) {
                return rank == '1st' || rank == '2nd' || rank == '3rd';
            };

            $scope.gainLoss = function (tx) {
                var cost = (tx.pricePaid || 0) * tx.quantity;
                var gross = (tx.priceSold || 0) * tx.quantity;
                var comissions = (tx.commision1 || 0) + (tx.commision2 || 0) + (tx.commission || 0);
                var net = gross - (cost + comissions);
                return net != 0 ? ( net / cost) * 100 : 0;
            };

            $scope.tropy = function (place) {
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

            $scope.tradingStart = function () {
                return (new Date()).getTime(); // TODO replace with service call
            };

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
            //              Private Methods
            //////////////////////////////////////////////////////////////////////

            function capitalize(value) {
                return value.charAt(0).toUpperCase() + value.substring(1);
            }

            function isContestSelected(contestId) {
                return ($scope.contest && $scope.contest._id.$oid == contestId);
            }

            function loadEnrichedParticipant(contestId, playerId) {
                ContestService.getParticipantByID(contestId, playerId)
                    .success(function (response) {
                        // TODO console.log("BEFORE participant = " + JSON.stringify($scope.participant, null, '\t'));
                        // TODO console.log("AFTER participant = " + JSON.stringify(response.data, null, '\t'));
                        $scope.participant = response.data;
                    })
                    .error(function (err) {

                    });
            }

            function lookupParticipantByName(userName) {
                if ($scope.contest) {
                    for(var n = 0; n < $scope.contest.participants.length; n++) {
                        var participant = $scope.contest.participants[n];
                        if(participant.name == userName) return participant;
                    }
                    return {};
                }
                else return {};
            }

            function placeName(n) {
                switch (n) {
                    case 1:
                        return "1st";
                    case 2:
                        return "2nd";
                    case 3:
                        return "3rd";
                    default:
                        return n + "th";
                }
            }

            /**
             * Updates a participant with pricing information
             */
            function updateWithPricing(participant) {
                // retrieve the symbols
                var symbols = [];
                if (participant.positions) {
                    for (var n = 0; n < participant.positions.length; n++) {
                        symbols.push(participant.positions[n].symbol);
                    }
                }

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
                ContestService.getRankings(contest._id.$oid).then(
                    function (response) {
                        contest.rankings = response.data;

                        // capture the rankings for the current player
                        if ($scope.participant) {
                            for (var n = 0; n < contest.rankings.length; n++) {
                                var ranking = contest.rankings[n];
                                if (ranking.name == playerName) {
                                    $scope.participant.gainLoss = ranking.gainLoss;
                                    $scope.participant.rank = ranking.rank;
                                }
                            }

                            // add empty slots if the contest is active
                            if (contest.status == 'ACTIVE' && contest.rankings) {
                                for (var m = contest.rankings.length; m < @maxPlayers; m++) {
                                    contest.rankings.push({
                                        name: null,
                                        rank: placeName(m + 1),
                                        totalEquity: contest.startingBalance,
                                        gainLoss: 0,
                                        score: 0
                                    });
                                }
                            }
                        }
                    },
                    function(err) {
                        $log.error("An error occurred loading rankings")
                    });
            }

            //////////////////////////////////////////////////////////////////////
            //              Event Listeners
            //////////////////////////////////////////////////////////////////////

            /**
             * Listen for contest deletion events
             */
            $scope.$on("contest_deleted", function (event, contest) {
                $log.info("Play/Base: Contest '" + contest.id + "' deleted");
                $log.info("contest.id = " + $scope.contest._id.$oid);

                // if the delete contest is selected, change the selection
                if (isContestSelected(contest.id)) {
                    $scope.contest = null;
                    MySession.contestId = null;
                }
            });

            /**
             * Listen for contest update events
             */
            $scope.$on("contest_updated", function (event, contest) {
                $log.info("Contest '" + contest.name + "' updated");
                insertID(contest);
                if(contest.id == MySession.contestId) {
                    $scope.contest = contest;
                }
            });

            // watch the contest ID
            $scope.$watch("MySession.contestId", function (oldVal, newVal) {
                // load the contest
                $scope.loadContest(MySession.contestId);
            });

        }]);