(function () {
    var app = angular.module('shocktrade');

    /**
     * Perks Dialog Service
     * @author lawrence.daniels@gmail.com
     */
    app.factory('PerksDialog', function ($http, $log, $modal, MySession) {
        var service = {};

        /**
         * Perks Modal Dialog
         */
        service.popup = function (params) {
            // create an instance of the dialog
            var $modalInstance = $modal.open({
                controller: 'PerksDialogController',
                templateUrl: 'perks_dialog.htm',
                resolve: {
                    params: function () {
                        return params;
                    }
                }
            });

            $modalInstance.result.then(function (result) {
                $log.info("result = " + angular.toJson(result));

            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };

        /**
         * Retrieves the complete list of perks
         * @returns {*}
         */
        service.getPerks = function () {
            return $http.get("/api/contest/" + MySession.getContestID() + "/perks");
        };

        /**
         * Retrieves the complete list of perks
         * @returns {*}
         */
        service.getMyPerks = function () {
            return $http.get("/api/contest/" + MySession.getContestID() + "/perks/" + MySession.getUserID());
        };

        /**
         * Attempts to purchase the given perk codes
         * @param contestId the given contest ID
         * @param playerId the given player ID
         * @param perkCodes the given perk codes to purchase
         * @returns {*}
         */
        service.purchasePerks = function (contestId, playerId, perkCodes) {
            return $http({
                method: 'PUT',
                url: "/api/contest/" + contestId + "/perks/" + playerId,
                data: angular.toJson(perkCodes)
            });
        };

        return service;
    });

    /**
     * Perks Dialog Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('PerksDialogController', ['$scope', '$http', '$log', '$modalInstance', 'toaster', 'MySession', 'PerksDialog',
        function ($scope, $http, $log, $modalInstance, toaster, MySession, PerksDialog) {
            var perks = [];
            var myFunds = 0;
            var myPerks = [];

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

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
                // the contest must be defined
                if (MySession.contestIsEmpty()) {
                    toaster.pop('error', "No game selected", null);
                    return;
                }

                // build the list of perks to purchase
                var perkCodes = getSelectedPerkCodes();
                var totalCost = getSelectedPerksCost();

                // send the purchase order
                PerksDialog.purchasePerks(MySession.getContestID(), MySession.getUserID(), perkCodes, totalCost)
                    .success(function (response) {
                        if (!response.error) {
                            toaster.pop('success', perkCodes.length + " Perk(s) purchased", null);
                        }
                        MySession.deductFundsAvailable(totalCost);
                        MySession.setContest(response);
                        $modalInstance.close(response);

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
                // load my perks
                PerksDialog.getMyPerks()
                    .success(function (response) {
                        $log.info("loadPerks: response = " + angular.toJson(response));
                        myFunds = response.fundsAvailable;
                        myPerks = response.perks;
                        $scope.setupPerks();
                    })
                    .error(function (error) {
                        toaster.pop('error', 'Error loading perks', null);
                    });


                // load all contest perks
                PerksDialog.getPerks()
                    .success(function (loadedPerks) {
                        perks = loadedPerks;
                        $scope.setupPerks();
                    })
                    .error(function (error) {
                        toaster.pop('error', 'Error loading perks', null);
                    });
            };

            $scope.setupPerks = function () {
                // create a mapping of the user's perks
                var ownedPerks = {};

                // all perks the user owns should be set
                angular.forEach(myPerks, function (perk) {
                    ownedPerks[perk] = true;
                });

                // setup the ownership for the perks
                angular.forEach(perks, function (perk) {
                    perk.owned = ownedPerks[perk.code] !== undefined;
                    perk.selected = perk.owned;
                });

                return ownedPerks;
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

            function getSelectedPerksCost() {
                var totalCost = 0.00;
                for (var n = 0; n < perks.length; n++) {
                    if (perks[n].selected && !perks[n].owned) {
                        totalCost += perks[n].cost;
                    }
                }
                return totalCost;
            }

        }]);

})();