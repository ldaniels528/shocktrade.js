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

            $scope.getCashAvailable = function (contest) {
                return ContestService.getCashAvailable(contest, MySession.getUserID());
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

            $scope.purchasePerks = function (contest) {
                // build the list of perks to purchase
                var perkCodes = getSelectedPerkCodes();
                $log.info("purchasePerks = " + JSON.stringify(perkCodes, null, '\t'));

                if (!contest) {
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

            $scope.getPerkCostClass = function (contest, perk) {
                if (perk.selected || $scope.getCashAvailable(contest) >= perk.cost) return 'positive';
                else if ($scope.getCashAvailable(contest) < perk.cost) return 'negative';
                else return 'null';
            };

            $scope.getPerkNameClass = function (contest, perk) {
                return ( perk.selected || $scope.getCashAvailable(contest) >= perk.cost ) ? 'st_bkg_color' : 'null';
            };

            $scope.getPerkDescClass = function (contest, perk) {
                return ( perk.selected || $scope.getCashAvailable(contest) >= perk.cost ) ? '' : 'null';
            };

            $scope.loadPerks = function (contest) {
                PerksService.getPerks(contest.OID())
                    .success(function (allPerks) {
                        perks = allPerks;
                        $scope.setupPerks(contest);
                    })
                    .error(function (error) {
                        toaster.pop('error', 'Error loading perks', null);
                    });
            };

            $scope.setupPerks = function (contest) {
                // create a mapping of the user's perks
                var myPerks = {};
                var player = ContestService.findPlayerByID(contest, MySession.getUserID());
                var userOwnedPerks = player ? (player.perks || []) : [];

                // all perks the user owns should be set
                angular.forEach(userOwnedPerks, function (perk) {
                    myPerks[perk] = true;
                });

                // setup the ownership for the perks
                angular.forEach(perks, function (perk) {
                    perk.owned = myPerks[perk.code] || false;
                    perk.selected = perk.owned;
                });
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

            $scope.$on("perks_updated", function (event, contestInfo) {
                $scope.setupPerks();
            });

        }]);

})();