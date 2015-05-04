(function () {
    var app = angular.module('shocktrade');

    /**
     * Perks Controller
     */
    app.controller('PerksCtrl', ['$scope', '$http', 'MySession', function ($scope, $http, MySession) {
            $scope.message = null;
            $scope.perks = [
                {
                    "code": "CREATOR",
                    "name": "Game Creator",
                    "cost": 3,
                    "description": "Gives the player the ability to  create new custom games"
                },
                {
                    "code": "PRCHEMNT",
                    "name": "Purchase Eminent",
                    "cost": 3,
                    "description": "Gives the player the ability to create SELL orders for securities not yet owned"
                },
                {
                    "code": "PRFCTIMG",
                    "name": "Perfect Timing",
                    "cost": 4,
                    "description": "Gives the player the ability to create BUY orders for more than cash currently available"
                },
                {
                    "code": "CMPDDALY",
                    "name": "Compounded Daily",
                    "cost": 5,
                    "description": "Gives the player the ability to earn interest on cash not currently invested"
                },
                {
                    "code": "FEEWAIVR",
                    "name": "Fee Waiver",
                    "cost": 5,
                    "description": "Reduces the commissions the player pays for buying or selling securities"
                },
                {
                    "code": "MARGIN",
                    "name": "Rational People think at the Margin",
                    "cost": 6,
                    "description": "Gives the player the ability to use margin accounts"
                },
                {
                    "code": "SAVGLOAN",
                    "name": "Savings and Loans",
                    "cost": 6,
                    "description": "Gives the player the ability to borrow money"
                },
                {
                    "code": "LOANSHRK",
                    "name": "Loan Shark",
                    "cost": 7,
                    "description": "Gives the player the ability to loan other players money at any interest rate"
                },
                {
                    "code": "MUTFUNDS",
                    "name": "The Feeling's Mutual",
                    "cost": 10,
                    "description": "Gives the player the ability to create and use mutual funds"
                },
                {
                    "code": "RISKMGMT",
                    "name": "Risk Management",
                    "cost": 12,
                    "description": "Gives the player the ability to trade options"
                }];

            $scope.purchasePerks = function () {
                // build the list of perks to purchase
                var purchasePerks = [];
                for (var n = 0; n < $scope.perks.length; n++) {
                    if ($scope.perks[n].selected && !$scope.perks[n].owned) {
                        purchasePerks.push($scope.perks[n].code);
                    }
                }
                console.log("purchasePerks = " + JSON.stringify(purchasePerks, null, '\t'));

                // send the purchase order
                $http({
                    method: 'PUT',
                    url: "/api/profile/" + MySession.getUserID() + "/perks/purchase",
                    data: angular.toJson(purchasePerks)
                }).success(function (response) {
                    $scope.message = purchasePerks.length + " Perk(s) purchased";
                    MySession.userProfile.perks = response;
                }).error(function (data, status, headers, config) {
                    console.log("Error: Purchase Perks " + data + "(" + status + ")");
                });
            };

            $scope.perksSelected = function () {
                var perks = $scope.perks;
                for (var n = 0; n < perks.length; n++) {
                    if (perks[n].selected && !perks[n].owned) return true;
                }
                return false;
            };

            $scope.pointsLeft = function () {
                var points = MySession.userProfile.perkPoints || 0;
                for (var n = 0; n < $scope.perks.length; n++) {
                    if ($scope.perks[n].selected && !$scope.perks[n].owned) {
                        points -= $scope.perks[n].cost;
                    }
                }
                return points;
            };

            $scope.getPerkCostClass = function (perk) {
                if (perk.selected || $scope.pointsLeft() >= perk.cost) return 'positive';
                else if ($scope.pointsLeft() < perk.cost) return 'negative';
                else return 'null';
            };

            $scope.getPerkNameClass = function (perk) {
                return ( perk.selected || $scope.pointsLeft() >= perk.cost ) ? 'perkName' : 'null';
            };

            $scope.getPerkDescClass = function (perk) {
                return ( perk.selected || $scope.pointsLeft() >= perk.cost ) ? '' : 'null';
            };

            $scope.setupPerks = function () {
                // create a mapping of the user's perks
                var myPerks = {};
                var userPerks = MySession.userProfile.perks || [];
                for (var n = 0; n < userPerks.length; n++) {
                    myPerks[userPerks[n]] = true;
                }

                // setup the ownership for the perks
                var perks = $scope.perks;
                for (var m = 0; m < perks.length; m++) {
                    perks[m].owned = myPerks[perks[m].code] || false;
                    perks[m].selected = perks[m].owned;
                }
            };

            //watch for changes to the player's perks
            $scope.$watch("MySession.userProfile.perks", function () {
                $scope.setupPerks();
            });

        }]);

})();