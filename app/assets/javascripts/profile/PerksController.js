(function () {
    var app = angular.module('shocktrade');

    /**
     * Perks Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('PerksController', ['$scope', '$http', '$log', 'toaster', 'MySession', 'PerksService',
        function ($scope, $http, $log, toaster, MySession, PerksService) {
            var perks = null;

            $scope.countOwnedPerks = function () {
                var count = 0;
                angular.forEach(perks, function (perk) {
                    if (perk.owned) count++;
                });
                return count;
            };

            $scope.getCashAvailable = function () {
                return MySession.getNetWorth();
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

                // send the purchase order
                PerksService.purchasePerks(perkCodes)
                    .success(function (response) {
                        toaster.pop('success', perkCodes.length + " Perk(s) purchased", null);
                        MySession.userProfile.perks = response.perks;
                        MySession.userProfile.netWorth = response.netWorth;
                        $scope.setupPerks();

                    }).error(function (data, status, headers, config) {
                        toaster.pop('error', "Failed to purchase " + perkCodes.length + " Perk(s)", null);
                        $log.error("Error: Purchase Perks " + data + "(" + status + ")");
                    });
            };

            $scope.getPerkCostClass = function (perk) {
                if (perk.selected || $scope.getCashAvailable() >= perk.cost) return 'positive';
                else if ($scope.getCashAvailable() < perk.cost) return 'negative';
                else return 'null';
            };

            $scope.getPerkNameClass = function (perk) {
                return ( perk.selected || $scope.getCashAvailable() >= perk.cost ) ? 'st_bkg_color' : 'null';
            };

            $scope.getPerkDescClass = function (perk) {
                return ( perk.selected || $scope.getCashAvailable() >= perk.cost ) ? '' : 'null';
            };

            $scope.loadPerks = function () {
                PerksService.getPerks()
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
                var userOwnedPerks = MySession.userProfile.perks || [];

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

            //watch for changes to the player's perks
            $scope.$watch("MySession.userProfile.perks", function () {
                $scope.setupPerks();
            });

            ///////////////////////////////////////////////////////////////////////////
            //          Initialization
            ///////////////////////////////////////////////////////////////////////////

            $scope.loadPerks();

        }]);

})();