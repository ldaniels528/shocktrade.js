(function () {
    var app = angular.module('shocktrade');

    /**
     * My Session Service
     * @author lawrence.daniels@gmail.com
     */
    app.factory('MySession', function ($rootScope, $log, $timeout, toaster, ContestService) {
        var service = {};

        /////////////////////////////////////////////////////////////////////
        //          Authentication & Authorization Functions
        /////////////////////////////////////////////////////////////////////

        /**
         * Returns the user ID for the current user's ID
         * @returns {*}
         */
        service.getUserID = function () {
            return service.userProfile ? service.userProfile.OID() : null;
        };

        /**
         * Returns the user ID for the current user's name
         * @returns {*}
         */
        service.getUserName = function () {
            return service.userProfile.name;
        };

        /**
         * Creates a default 'Spectator' user profile
         * @returns {{name: string, country: string, level: number, lastSymbol: string, friends: Array, filters: *[]}}
         */
        function createSpectatorProfile() {
            return {
                name: "Spectator",
                country: "us",
                level: 1,
                lastSymbol: "MSFT",
                friends: []
            }
        }

        /**
         * Indicates whether the given user is an administrator
         * @returns {boolean}
         */
        service.isAdmin = function () {
            return service.userProfile.admin === true;
        };

        /**
         * Indicates whether the user is logged in
         * @returns {boolean}
         */
        service.isAuthorized = function () {
            return service.getUserID() != null;
        };

        /**
         * Logout function
         */
        service.logout = function () {
            service.authenticated = false;
            service.fbAuthenticated = false;
            service.fbUserID = null;
            service.fbFriends = [];
            service.fbProfile = {};
            service.userProfile = createSpectatorProfile();
        };

        /////////////////////////////////////////////////////////////////////
        //          NetWorth Functions
        /////////////////////////////////////////////////////////////////////

        service.deduct = function (amount) {
            $log.info("Deducting " + amount + " from " + service.userProfile.netWorth);
            service.userProfile.netWorth -= amount;
        };

        service.getNetWorth = function () {
            return service.userProfile.netWorth || 0;
        };

        service.getTotalCashAvailable = function () {
            return service.userProfile ? service.userProfile.netWorth : 0.00;
        };

        service.getTotalInvestment = function () {
            // lookup the player
            if (!service.isTotalInvestmentLoaded() && !service.totalInvestmentStatus) {
                service.totalInvestmentStatus = "LOADING";

                // load the total investment
                service.loadTotalInvestment();
            }

            return service.isTotalInvestmentLoaded() ? service.totalInvestment : null;
        };

        service.isTotalInvestmentLoaded = function () {
            return service.totalInvestment !== null && service.totalInvestment !== undefined;
        };

        service.reloadTotalInvestment = function () {
            service.totalInvestmentStatus = null;
        };

        service.loadTotalInvestment = function () {
            // set a timeout so that loading doesn't persist
            $timeout(function () {
                if (!service.isTotalInvestmentLoaded()) {
                    $log.error("Total investment call timed out");
                    service.totalInvestmentStatus = "TIMEOUT";
                }
            }, 20000);

            // retrieve the total investment
            $log.info("Loading Total investment...");
            ContestService.getTotalInvestment(service.getUserID())
                .success(function (response) {
                    service.totalInvestment = response.netWorth;
                    service.totalInvestmentStatus = "LOADED";
                    $log.info("Total investment loaded");
                })
                .error(function (response) {
                    toaster.pop('error', 'Error loading total investment', null);
                    service.totalInvestmentStatus = "FAILED";
                    $log.error("Total investment call failed");
                });
        };

        /////////////////////////////////////////////////////////////////////
        //          Contest Functions
        /////////////////////////////////////////////////////////////////////

        service.contestIsEmpty = function () {
            return service.getContestID() === null;
        };

        service.getContest = function () {
            return service.contest;
        };

        service.getContestID = function () {
            return service.contest.OID();
        };

        service.getContestStatus = function () {
            return service.contest.status;
        };

        service.setContest = function (contest) {
            if (!contest) service.resetContest();
            else {
                service.contest = contest;
                service.participant = getParticipant(contest);
            }
        };

        service.getFundsAvailable = function () {
            return service.participant.fundsAvailable || 0.00;
        };

        service.setMessages = function (messages) {
            service.contest.messages = messages;
        };

        service.getMessages = function () {
            return service.contest.messages;
        };

        service.getOrders = function () {
            return service.participant.orders;
        };

        service.getOrderHistory = function () {
            return service.participant.orderHistory;
        };

        service.getPerformance = function () {
            return service.participant.performance;
        };

        service.getPositions = function () {
            return service.participant.positions;
        };

        service.resetContest = function () {
            service.contest = {};
            service.participant = {};
        };

        function getParticipant(contest) {
            return ContestService.findPlayerByID(contest, service.getUserID()) || {};
        }

        ////////////////////////////////////////////////////////////
        //          Watch Events
        ////////////////////////////////////////////////////////////

        function updateContestDelta(contest) {
            // update the messages (if present)
            if (contest.messages) {
                service.contest.messages = contest.messages;
            }

            // lookup our participant
            var participant = getParticipant(contest);
            if (participant) {
                // update funds available (if present)
                if (participant.fundsAvailable !== undefined) {
                    service.participant.fundsAvailable = participant.fundsAvailable;
                }

                // update the orders (if present)
                if (participant.orders) {
                    service.participant.orders = participant.orders;
                }

                // update the order history (if present)
                if (participant.orderHistory) {
                    service.participant.orderHistory = participant.orderHistory;
                }

                // update the perks (if present)
                if (participant.perks) {
                    service.participant.perks = participant.perks;
                }

                // update the positions (if present)
                if (participant.positions) {
                    service.participant.positions = participant.positions;
                }

                // update the performance (if present)
                if (participant.performance) {
                    service.participant.performance = participant.performance;
                }
            }
        }

        $rootScope.$on("contest_deleted", function (event, contest) {
            if (service.getContestID() === contest.OID()) {
                service.resetContest();
            }
        });

        $rootScope.$on("contest_updated", function (event, contest) {
            $log.info("[MySession] Contest '" + contest.name + "' updated");
            service.contest = contest;
        });

        $rootScope.$on("messages_updated", function (event, contest) {
            $log.info("[MySession] Messages for Contest '" + contest.name + "' updated");
            updateContestDelta(contest);
        });

        $rootScope.$on("orders_updated", function (event, contest) {
            $log.info("[MySession] Orders for Contest '" + contest.name + "' updated");
            updateContestDelta(contest);
        });

        $rootScope.$on("perks_updated", function (event, contest) {
            $log.info("[MySession] Perks for Contest '" + contest.name + "' updated");
            updateContestDelta(contest);
        });

        $rootScope.$on("positions_updated", function (event, contest) {
            $log.info("[MySession] Positions for Contest '" + contest.name + "' updated");
            updateContestDelta(contest);
        });

        ////////////////////////////////////////////////////////////
        //          Initialization
        ////////////////////////////////////////////////////////////

        (function () {
            // make sure we're starting fresh
            service.resetContest();

            // initialize the values as a logged-out user
            service.logout();
        })();

        return service;
    });

})();