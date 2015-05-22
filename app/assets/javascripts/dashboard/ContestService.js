(function () {
    var app = angular.module('shocktrade');

    /**
     * Contest Service
     * @author lawrence.daniels@gmail.com
     */
    app.factory('ContestService', function ($http, $log, $q, toaster, QuoteService) {
        var service = {};

        ///////////////////////////////////////////////////////////////
        //          Contest C.R.U.D.
        ///////////////////////////////////////////////////////////////

        service.createContest = function (form) {
            return $http({method: "PUT", url: "/api/contest", data: angular.toJson(form)});
        };

        service.deleteContest = function (contestId) {
            return $http.delete('/api/contest/' + contestId);
        };

        service.joinContest = function (contestId, playerInfo) {
            return $http({
                method: 'PUT',
                url: "/api/contest/" + contestId + "/player",
                data: angular.toJson(playerInfo)
            });
        };

        service.quitContest = function (contestId, playerId) {
            return $http.delete('/api/contest/' + contestId + "/player/" + playerId);
        };

        service.startContest = function (contestId) {
            return $http.get("/api/contest/" + contestId + "/start");
        };

        ///////////////////////////////////////////////////////////////
        //          Contest Finders
        ///////////////////////////////////////////////////////////////

        service.findContests = function (searchOptions) {
            if(!searchOptions) throw "SearchOptions is null or undefined";
            else {
                return $http({method: 'POST', url: '/api/contests/search', data: angular.toJson(searchOptions)})
                    .then(function (response) {
                        return response.data;
                    });
            }
        };

        service.getContestByID = function (contestId) {
            if(!contestId) throw "getContestByID: Contest ID is null or undefined";
            else return $http.get("/api/contest/" + contestId);
        };

        service.getParticipantByID = function (contestId, playerId) {
            if(!contestId) throw "getParticipantByID: Contest ID is null or undefined";
            else if(!playerId) throw "getParticipantByID: Player ID is null or undefined";
            else return $http.get("/api/contest/" + contestId + "/player/" + playerId);
        };

        service.getCashAvailable = function (contest, playerId) {
            if(contest) {
                var player = service.findPlayerByID(contest, playerId);
                return player ? player.fundsAvailable : 0.00;
            }
            return 0.00;
        };

        service.getRankings = function (contestId) {
            if(!contestId) throw "getRankings: Contest ID is null or undefined";
            else return $http.get("/api/contest/" + contestId + "/rankings");
        };

        service.getContestsByPlayerID = function (playerId) {
            if(!playerId) throw "getContestsByPlayerID: Player ID is null or undefined";
            else return $http.get('/api/contests/player/' + playerId);
        };

        service.getEnrichedOrders = function (contestId, playerId) {
            if(!contestId) throw "getEnrichedOrders: Contest ID is null or undefined";
            else if(!playerId) throw "getEnrichedOrders: Player ID is null or undefined";
            else return $http.get('/api/contest/' + contestId + '/orders/' + playerId);
        };

        service.getEnrichedPositions = function (contestId, playerId) {
            if(!contestId) throw "getEnrichedPositions: Contest ID is null or undefined";
            else if(!playerId) throw "getEnrichedPositions: Player ID is null or undefined";
            else return $http.get('/api/contest/' + contestId + '/positions/' + playerId);
        };

        /////////////////////////////////////////////////////////////////////////////
        //			Participants
        /////////////////////////////////////////////////////////////////////////////

        service.getTotalInvestment = function (playerId) {
            if(!playerId) throw "getTotalInvestment: Player ID is null or undefined";
            else return $http.get('/api/contests/player/' + playerId + '/totalInvestment');
        };

        service.findPlayerByID = function (contest, playerId) {
            var participants = contest ? (contest.participants || []) : [];
            for (var n = 0; n < participants.length; n++) {
                var participant = participants[n];
                if (participant.OID() === playerId) {
                    return participant;
                }
            }
            return null;
        };

        service.findPlayerByName = function (contest, playerName) {
            var participants = contest ? (contest.participants || []) : [];
            for (var n = 0; n < participants.length; n++) {
                var participant = participants[n];
                if (participant.name === playerName) {
                    return participant;
                }
            }
            return null;
        };

        service.getPlayerRankings = function (contest, playerName) {
            if (!contest || !contest.name) return [];
            else {
                // if the rankings have never been loaded ...
                if (contest.rankings === undefined) {
                    contest.rankings = {};
                    $log.info("Loading Contest Rankings for '" + contest.name + "'...");
                    service.getRankings(contest.OID())
                        .success(function (participants) {
                            contest.rankings.participants = participants;
                            if (participants.length) {
                                contest.rankings.leader = participants[0];
                                contest.rankings.player = playerName ? service.findPlayerByName(contest.rankings, playerName) : null;
                            }
                        })
                        .error(function (response) {
                            toaster.pop('error', 'Error!', "Error loading play rankings");
                            $log.error(response.error)
                        });
                }

                // if the rankings were loaded, but the player is not set
                else if(playerName && contest.rankings.player == null) {
                    contest.rankings.player = playerName ? service.findPlayerByName(contest.rankings, playerName) : null;
                }

                return contest.rankings;
            }
        };

        /////////////////////////////////////////////////////////////////////////////
        //			Miscellaneous
        /////////////////////////////////////////////////////////////////////////////

        service.getChart = function (contestId, participantName, chartName) {
            // build the appropriate URL
            var uriString = (chartName == "gains" || chartName == "losses")
                ? "/api/charts/performance/" + chartName + "/" + contestId + "/" + participantName
                : "/api/charts/exposure/" + chartName + "/" + contestId + "/" + participantName;

            // load the chart representing the securities
            return $http.get(uriString).then(function (response) {
                return response.data;
            });
        };

        /////////////////////////////////////////////////////////////////////////////
        //			Chat
        /////////////////////////////////////////////////////////////////////////////

        service.sendChatMessage = function (contestId, message) {
            return $http({
                method: 'PUT',
                url: '/api/contest/' + contestId + '/chat',
                data: angular.toJson(message)
            });
        };

        /////////////////////////////////////////////////////////////////////////////
        //			Positions & Orders
        /////////////////////////////////////////////////////////////////////////////

        service.createOrder = function (contestId, playerId, order) {
            return $http({
                method: "PUT",
                url: "/api/order/" + contestId + "/" + playerId,
                data: angular.toJson(order)
            });
        };

        service.deleteOrder = function (contestId, playerId, orderId) {
            return $http.delete('/api/order/' + contestId + '/' + playerId + '/' + orderId);
        };

        service.getHeldSecurities = function (playerId) {
            return $http.get("/api/positions/" + playerId);
        };

        service.orderQuote = function (symbol) {
            $log.info("Loading symbol " + symbol + "'...");
            return $http.get('/api/quotes/order/symbol/' + symbol)
                .then(function (response) {
                    var quote = response.data;
                    if (quote.symbol) {
                        $log.info("Setting lastSymbol as " + quote.symbol);
                        QuoteService.lastSymbol = quote.symbol;
                    }
                    return quote;
                });
        };

        return service;
    });

})();