(function () {
    var app = angular.module('shocktrade');

    /**
     * Perks Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('PerksController', ['$scope', '$http', '$log', 'toaster', 'ContestService', 'MySession', 'PerksService',
        function ($scope, $http, $log, toaster, ContestService, MySession, PerksService) {
            var perks = [];

            $scope.countOwnedPerks = function () {
                var count = 0;
                angular.forEach(perks, function (perk) {
                    if (perk.owned) count++;
                });
                return count;
            };

            $scope.hasSufficientFunds = function () {
                return $scope.getTotalCost() <= MySession.getFundsAvailable();
            };

            $scope.getPerks = function () {
                return perks;
            };

            $scope.getTotalCost = function () {
                var totalCost = 0.00;
                angular.forEach(perks, function (perk) {
                    if (perk.selected && !perk.owned) {
                        totalCost += perk.cost;
                    }
                });
                return totalCost;
            };

            $scope.perksSelected = function () {
                if (perks) {
                    for (var n = 0; n < perks.length; n++) {
                        if (perks[n].selected && !perks[n].owned) return true;
                    }
                }
                return false;
            };

            $scope.purchasePerks = function () {
                // build the list of perks to purchase
                var perkCodes = getSelectedPerkCodes();
                $log.info("purchasePerks = " + JSON.stringify(perkCodes, null, '\t'));

                var contest = MySession.contest;
                if (!contest || !contest.OID()) {
                    toaster.pop('error', "No game selected", null);
                    return;
                }

                // send the purchase order
                PerksService.purchasePerks(contest.OID(), MySession.getUserID(), perkCodes)
                    .success(function (response) {
                        if (response.error) {
                            toaster.pop('error', response.error, null);
                        }
                        else {
                            toaster.pop('success', perkCodes.length + " Perk(s) purchased", null);
                            $scope.setupPerks();
                        }

                    }).error(function (data, status, headers, config) {
                        toaster.pop('error', "Failed to purchase " + perkCodes.length + " Perk(s)", null);
                        $log.error("Error: Purchase Perks " + data + "(" + status + ")");
                    });
            };

            $scope.getPerkCostClass = function (perk) {
                if (perk.selected || MySession.getFundsAvailable() >= perk.cost) return 'positive';
                else if (MySession.getFundsAvailable() < perk.cost) return 'negative';
                else return 'null';
            };

            $scope.getPerkNameClass = function (perk) {
                return ( perk.selected || MySession.getFundsAvailable() >= perk.cost ) ? 'st_bkg_color' : 'null';
            };

            $scope.getPerkDescClass = function (perk) {
                return ( perk.selected || MySession.getFundsAvailable() >= perk.cost ) ? '' : 'null';
            };

            $scope.loadPerks = function () {
                PerksService.getPerks(MySession.getContestID())
                    .success(function (allPerks) {
                        perks = allPerks;
                        $scope.setupPerks();
                    })
                    .error(function (error) {
                        toaster.pop('error', 'Error loading perks', null);
                    });
            };

            $scope.setupPerks = function () {
                // create a mapping of the user's perks
                var myPerks = {};

                // all perks the user owns should be set
                angular.forEach(MySession.participant.perks, function (perk) {
                    myPerks[perk] = true;
                });

                // setup the ownership for the perks
                angular.forEach(perks, function (perk) {
                    perk.owned = myPerks[perk.code] || false;
                    perk.selected = perk.owned;
                });

                return myPerks;
            };

            ///////////////////////////////////////////////////////////////////////////
            //          Private Functions
            ///////////////////////////////////////////////////////////////////////////

            function getSelectedPerkCodes() {
                var perkCodes = [];
                for (var n = 0; n < perks.length; n++) {
                    if (perks[n].selected && !perks[n].owned) {
                        perkCodes.push(perks[n].code);
                    }
                }
                return perkCodes;
            }

            ///////////////////////////////////////////////////////////////////////////
            //          Watch Events
            ///////////////////////////////////////////////////////////////////////////

            $scope.$on("perks_updated", function (event, contest) {
                $scope.setupPerks();
            });

        }]);

})();