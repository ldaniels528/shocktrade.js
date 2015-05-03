/**
 * Game Play Controller
 */
(function () {
    angular
        .module('shocktrade')
        .controller('GameCtrl', ['$rootScope', '$scope', '$location', '$log', '$timeout', 'MySession', 'ContestService', 'Errors', 'InvitePlayerDialog', 'NewGameDialog', 'NewOrderDialog', 'QuoteService',
            function ($rootScope, $scope, $location, $log, $timeout, MySession, ContestService, Errors, InvitePlayerDialog, NewGameDialog, NewOrderDialog, QuoteService) {

                // setup the current contest
                $scope.contest = null;

                // setup contest variables
                $scope.myContests = [];
                $scope.searchResults = [];
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

                // setup UI variables
                $scope.statusBar = "";
                $scope.selection = {
                    exposure: "sector",
                    performance: "gains"
                };

                // setup contest variables
                $scope.orderChoice = "active";
                $scope.selectedPosition = null;

                $scope.popupNewGameDialog = function () {
                    NewGameDialog.popup({});
                };

                $scope.popupNewOrderDialog = function () {
                    NewOrderDialog.popup({symbol: QuoteService.lastSymbol});
                };

                $scope.sellPosition = function (symbol, quantity) {
                    NewOrderDialog.popup({symbol: symbol, quantity: quantity});
                };

                $scope.getAvailableCount = function () {
                    var count = 0;
                    $scope.searchResults.forEach(function (r) {
                        if (r.status === 'ACTIVE') count++;
                    });
                    return count;
                };

                $scope.getAvailableSlots = function () {
                    if ($scope.contest == null) return [];
                    else {
                        var slots = [];
                        var count = $scope.maxPlayers - $scope.contest.participants.length;
                        for (var n = 0; n < count; n++) {
                            slots.push(n);
                        }
                        return slots;
                    }
                };

                $scope.getStatusIcon = function (c) {
                    if(c && c.invitationOnly) return "/assets/images/objects/locked.png";
                    else {
                        var playerCount = ((c && c.participants) || []).length;
                        if (playerCount + 1 < $scope.maxPlayers) return "/assets/images/status/greenlight.png";
                        else if (playerCount + 1 === $scope.maxPlayers) return "/assets/images/status/yellowlight.gif";
                        else if (playerCount >= $scope.maxPlayers) return "/assets/images/status/redlight.png";
                        else return "/assets/images/status/offlight.png";
                    }
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

                    ContestService.findContests(searchOptions).then(
                        function (contests) {
                            $scope.searchResults = contests;
                            $scope.stopLoading();
                        },
                        function (err) {
                            Errors.addMessage("Failed to execute Contest Search");
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

                $scope.isContestOwner = function (contest) {
                    return contest && contest.creator.name === MySession.getUserName();
                };

                $scope.isParticipant = function (contest) {
                    var id = MySession.getUserID();
                    return id && findPlayerByID(contest, id) != null;
                };

                $scope.deleteContest = function (contest) {
                    contest.deleting = true;
                    ContestService.deleteContest(contest._id.$oid)
                        .success(function (response) {
                            removeContestFromList(contest.id);
                            $timeout(function () {
                                contest.deleting = false;
                            }, 500);
                        })
                        .error(function (err) {
                            Errors.addMessage("Failed to delete contest");
                            $log.error("An error occurred while deleting the contest");
                            $timeout(function () {
                                contest.deleting = false;
                            }, 500);
                        });
                };

                $scope.isJoinable = function(contest) {
                    return contest && !contest.invitationOnly && !$scope.isContestOwner(contest) && !$scope.isParticipant(contest);
                };

                $scope.joinContest = function (contest) {
                    contest.joining = true;
                    ContestService.joinContest(contest._id.$oid, {
                        player: {
                            "id": MySession.getUserID(),
                            "name": MySession.getUserName(),
                            "facebookID": MySession.fbUserID
                        }
                    }).success(function (contest) {
                        if (!contest.error) {
                            $scope.contest = contest;
                            updateWithRankings(MySession.getUserName(), contest)
                        }
                        $timeout(function () {
                            contest.joining = false;
                        }, 500);
                    })
                        .error(function (err) {
                            Errors.addMessage("Failed to join contest");
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
                    ContestService.quitContest(contest._id.$oid, MySession.getUserID())
                        .success(function (updatedContest) {
                            if (!updatedContest.error) {
                                $scope.contest = updatedContest;
                            }
                            else {
                                $log.error("error = " + updatedContest.error);
                                Errors.addMessage("Unable to process your quit command at this time.")
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
                    ContestService.startContest(contest._id.$oid)
                        .success(function (contest) {
                            if (contest.error) {
                                Errors.addMessage(contest.error);
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
                    $log.info("Changing location to " + $scope.playTabs[tabIndex].url);
                    $location.path($scope.playTabs[tabIndex].url);
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

                $scope.popupInvitePlayerDialog = function (participant) {
                    InvitePlayerDialog.popup($scope, participant);
                };

                $scope.containsPlayer = function (contest, userProfile) {
                    return userProfile.id && findPlayerByID(contest, userProfile.id) != null;
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
                        $log.info("Changing contest to " + contest._id.$oid);
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
                    $scope.getChart(MySession.contestId, MySession.getUserName(), $scope.selection.exposure, target);
                };

                $scope.getPerformanceChart = function (target) {
                    $scope.getChart(MySession.contestId, MySession.getUserName(), $scope.selection.performance, target);
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
                                $log.info("Error: " + angular.toJson(err));
                            });
                    }
                };

                $scope.loadContest = function (contestId) {
                    if (contestId) {
                        // load the contest
                        ContestService.getContestByID(contestId)
                            .success(function (contest) {
                                $scope.contest = contest;
                                $scope.orderChoice = contest.status == 'ACTIVE' ? "active" : "history";
                                MySession.contestId = contestId;

                                // cache the player's name
                                var playerName = MySession.getUserName();

                                // find participant that represents the player
                                $scope.participant = findPlayerByID(contest, MySession.getUserID());

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

                $scope.toggleSplitScreen = function () {
                    $scope.splitScreen = false;
                };

                $scope.isSplitScreen = function () {
                    return $scope.splitScreen && $scope.contest != null;
                };

                $scope.selectContest = function (contest) {
                    $log.info("Selecting contest '" + contest.name + "' (" + contest._id.$oid + ")");
                    $scope.contest = contest;
                    $scope.participant = findPlayerByID(contest, MySession.getUserID());
                    MySession.contestId = contest._id.$oid;
                    $scope.splitScreen = true;

                    enrichContest(contest);
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

                $scope.tradingStart = function () {
                    return (new Date()).getTime(); // TODO replace with service call
                };

                $scope.getParticipant = function(contest) {
                    if($scope.participant == null) {
                        $scope.participant = findPlayerByID(contest, MySession.getUserID());
                        enrichParticipant(contest, $scope.participant);
                    }
                    return $scope.participant;
                };

                $scope.getActiveOrders = function(contest) {
                    var participant = $scope.getParticipant(contest);
                    return participant ? (participant.orders || []) : [];
                };

                $scope.getClosedOrders = function(contest) {
                    var participant = $scope.getParticipant(contest);
                    return participant ? (participant.orderHistory || []) : [];
                };

                $scope.cancelOrder = function (contestId, playerId, orderId) {
                    ContestService.deleteOrder(contestId, playerId, orderId)
                        .success(function (participant) {
                            $scope.participant = participant;
                            updateWithPricing(participant);
                        })
                        .error(function (err) {
                            Errors.addMessage("Failed to cancel order");
                        });
                };

                $scope.getPositions = function(contest) {
                    var participant = $scope.getParticipant(contest);
                    return participant ? (participant.positions || []) : [];
                };

                $scope.isPositionSelected = function() {
                    return $scope.selectedPosition != null;
                };

                $scope.selectPosition = function(position) {
                    $scope.selectedPosition = position;
                };

                $scope.toggleSelectedPosition = function () {
                    $scope.selectedPosition = null;
                };

                $scope.getCashAvailable = function (participant) {
                    return participant ? (participant.fundsAvailable || 0) - $scope.getTotalBuyOrders(participant) : 0;
                };

                $scope.asOfDate = function (participant) {
                    return participant && participant.lastTradeTime ? participant.lastTradeTime : new Date();
                };

                $scope.getTotalOrders = function (participant) {
                    return $scope.getTotalBuyOrders(participant) + $scope.getTotalSellOrders(participant);
                };

                $scope.getTotalEquity = function (participant) {
                    return $scope.getTotalInvestment(participant) + (participant ? (participant.fundsAvailable || 0) : 0);
                };

                $scope.getTotalInvestment = function (participant) {
                    var positions = ((participant || {}).positions) || [];
                    var total = 0;
                    for (var n = 0; n < positions.length; n++) {
                        var p = positions[n];
                        total += p.price * p.quantity + p.commission;
                    }
                    return total;
                };

                $scope.getTotalBuyOrders = function (participant) {
                    var orders = ((participant || {}).orders) || [];
                    var total = 0;
                    angular.forEach(orders, function (o) {
                        if (o.orderType == 'BUY') {
                            total += o.price * o.quantity + o.commission;
                        }
                    });
                    return total;
                };

                $scope.getTotalSellOrders = function (participant) {
                    var orders = ((participant || {}).orders) || [];
                    var total = 0;
                    angular.forEach(orders, function (o) {
                        if (o.orderType == 'SELL') {
                            total += o.price * o.quantity + o.commission;
                        }
                    });
                    return total;
                };

                $scope.isMarketOrder = function (order) {
                    return order.priceType == 'MARKET' || order.priceType == 'MARKET_ON_CLOSE';
                };

                //////////////////////////////////////////////////////////////////////
                //              Private Methods
                //////////////////////////////////////////////////////////////////////

                function capitalize(value) {
                    return value.charAt(0).toUpperCase() + value.substring(1);
                }

                function enrichContest(contest) {
                    if (!contest.rankings) {
                        updateWithRankings(MySession.getUserName(), contest);
                        if (MySession.getUserID()) {
                            loadEnrichedParticipant(MySession.contestId, MySession.getUserID());
                        }
                    }
                }

                function enrichParticipant(contest, participant) {
                    ContestService.getEnrichedPositions(contest._id.$oid, participant._id.$oid)
                        .success(function(response) {
                            var enrichedPosition = response[0];
                            $log.info("Loaded enriched player " + angular.toJson(enrichedPosition));

                            for(var n = 0; n < participant.positions.length; n++) {
                                var p = participant.positions[n];
                                if(p._id.$oid === enrichedPosition._id.$oid) {
                                    participant.enriched = true;
                                    participant.positions[n] = enrichedPosition;
                                    return;
                                }
                            }
                        })
                        .error(function(err) {
                            Errors.addMessage("Error loading player positions");
                        });
                }

                function findPlayerByID(contest, playerId) {
                    var participants = contest ? contest.participants : null;
                    if (participants) {
                        for (var n = 0; n < participants.length; n++) {
                            var participant = participants[n];
                            if (participant._id.$oid == playerId) {
                                return participant;
                            }
                        }
                    }
                    return null;
                }

                function isContestSelected(contestId) {
                    return ($scope.contest && $scope.contest._id.$oid === contestId);
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
                        ContestService.getRankings(contest._id.$oid).then(function (response) {
                            $log.info("Loading rankings for contest " + contest._id.$oid + "...");
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
                            $log.info("Loading rankings for contest " + contest._id.$oid + " for player " + playerName + "...");
                            ContestService.getRankings(contest._id.$oid).then(function (response) {
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

                    // if the delete contest is selected, change the selection
                    if (isContestSelected(contestId)) {
                        $scope.contest = null;
                        $scope.splitScreen = false;
                        MySession.contestId = null;
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
                    ContestService.getRankings(contest._id.$oid).then(
                        function (response) {
                            contest.rankings = response.data;
                            if(contest.rankings.length) {
                                contest.leader = contest.rankings[0];
                            }

                            // capture the rankings for the current player
                            if ($scope.participant) {
                                for (var n = 0; n < contest.rankings.length; n++) {
                                    var ranking = contest.rankings[n];
                                    if (ranking.name === playerName) {
                                        $scope.participant.gainLoss = ranking.gainLoss;
                                        $scope.participant.rank = ranking.rank;
                                    }
                                }
                            }
                        },
                        function (err) {
                            $log.error("An error occurred loading rankings")
                        });
                }

                function updateWithRankingsForContests(contests) {
                    if (contests.length) {
                        $log.info("Preparing to load rankings for " + contests.length + " contest(s)");

                        // load the rankings for the current player or leader
                        var playerName = MySession.getUserName();
                        if (playerName != 'Spectator') {
                            loadPlayerRankings(contests, playerName);
                        }
                        else {
                            loadLeaderRankings(contests);
                        }
                    }
                }

                //////////////////////////////////////////////////////////////////////
                //              Data Graphs
                //////////////////////////////////////////////////////////////////////

                // setup the tabs
                $scope.playTabs = [{
                    "name": "Search",
                    "imageURL": "/assets/images/buttons/search.png",
                    "path": "/assets/views/play/search/search.htm",
                    "url": "/play/search",
                    "active": false,
                    "lockable": false,
                    "isVisible": function (c) {
                        return true;
                    }
                }, {
                    "name": "Lobby",
                    "imageURL": "/assets/images/objects/home.gif",
                    "path": "/assets/views/play/lobby/lobby.htm",
                    "url": "/play/lobby",
                    "active": false,
                    "lockable": false,
                    "isVisible": function (c) {
                        return c && c._id;
                    }
                }, {
                    "name": "Lounge",
                    "imageURL": "/assets/images/objects/friend_header.gif",
                    "path": "/assets/views/play/lounge/lounge.htm",
                    "url": "/play/lounge",
                    "active": false,
                    "lockable": true,
                    "disabled": false,
                    "isVisible": function (c) {
                        return c && c._id;
                    }
                }, {
                    "name": "Awards",
                    "path": "/assets/views/play/awards/awards.htm",
                    "imageURL": "/assets/images/objects/award.gif",
                    "url": "/play/awards",
                    "active": false,
                    "lockable": false,
                    "isVisible": function (c) {
                        return true;
                    }
                }, {
                    "name": "Perks",
                    "path": "/assets/views/play/perks/perks.htm",
                    "imageURL": "/assets/images/objects/gift.png",
                    "url": "/play/perks",
                    "active": false,
                    "lockable": false,
                    "disabled": false,
                    "isVisible": function (c) {
                        return true;
                    }
                }, {
                    "name": "Statistics",
                    "path": "/assets/views/play/statistics/statistics.htm",
                    "imageURL": "/assets/images/objects/stats.gif",
                    "url": "/play/statistics",
                    "active": false,
                    "lockable": false,
                    "isVisible": function (c) {
                        return true;
                    }
                }];

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
                    insertID(contest);
                    $scope.searchResults.push(contest);
                });

                /**
                 * Listen for contest deletion events
                 */
                $scope.$on("contest_deleted", function (event, contest) {
                    removeContestFromList(contest.id);
                });

                /**
                 * Listen for contest update events
                 */
                $scope.$on("contest_updated", function (event, contest) {
                    $log.info("Contest '" + contest.name + "' updated");
                    insertID(contest);
                    if (contest.id === MySession.contestId) {
                        $scope.contest = contest;
                    }
                });

                /**
                 * Listen for contest update events
                 */
                $scope.$on("contest_updated", function (event, contest) {
                    $log.info("Contest '" + contest.name + "' updated");
                    insertID(contest);
                });

                //////////////////////////////////////////////////////////////////////
                //              Watch Event Listeners
                //////////////////////////////////////////////////////////////////////

                // watch for changes to the player's profile
                $scope.$watch("MySession.userProfile", function (oldVal, newVal) {
                    var playerName = MySession.getUserName();
                    if (playerName !== 'Spectator') {
                        // load the player's games
                        $scope.loadContestsByPlayerID(MySession.getUserID());
                    }
                });

                $scope.$watch("searchOptions", function () {
                    $scope.contestSearch($scope.searchOptions);
                }, true);

            }]);
})();