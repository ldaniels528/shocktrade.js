/**
 * New Game Dialog Service and Controller
 * @author lawrence.daniels@gmail.com
 */
(function () {
    var app = angular.module('shocktrade');

    /**
     * New Game Dialog Singleton
     * @author lawrence.daniels@gmail.com
     */
    app.factory('NewGameDialog', function ($http, $log, $modal, $q, ContestService) {
        var service = {};

        /**
         * Sign-up Modal Dialog
         */
        service.popup = function (params) {
            // create an instance of the dialog
            var $modalInstance = $modal.open({
                controller: 'NewGameDialogCtrl',
                templateUrl: 'new_game_dialog.htm',
                resolve: {
                    params: function () {
                        return params;
                    }
                }
            });

            $modalInstance.result.then(function (form) {
                service.createNewGame(form).then(
                    function (response) {
                        if (params.success) params.success(response);
                    },
                    function (err) {
                        if(params.error) params.error(err);
                    });
            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };

        service.createNewGame = function (form) {
            $log.info("Creating new game " + angular.toJson(form));
            var deferred = $q.defer();
            ContestService.createContest(form)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function (response) {
                    deferred.reject(response);
                });
            return deferred.promise;
        };

        return service;
    });

    /**
     * New Game Dialog Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('NewGameDialogCtrl', ['$rootScope', '$scope', '$http', '$log', '$modalInstance', 'ContestService', 'MySession',
        function ($rootScope, $scope, $http, $log, $modalInstance, ContestService, MySession) {
            $scope.durations = [
                {label: "1 Week", value: 7},
                {label: "2 Weeks", value: 14},
                {label: "1 Month", value: 30}
            ];
            $scope.startingBalances = [
                1000, 2500, 5000, 10000, 25000, 50000, 100000
            ];
            $scope.restrictionTypes = [];
            $scope.form = {
                duration: null,
                perksAllowed: true,
                robotsAllowed: true,
                startAutomatically: true,
                startingBalance: $scope.startingBalances[0]
            };
            $scope.errors = [];

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

            $scope.create = function () {
                // add the player info
                $scope.form.player = {
                    id: $rootScope.MySession.getUserID(),
                    name: $rootScope.MySession.getUserName(),
                    facebookID: $rootScope.MySession.getFacebookID()
                };

                $log.info("form = " + angular.toJson($scope.form));
                if (isValidForm()) {
                    $modalInstance.close($scope.form);
                }
            };

            $scope.enforceInvitationOnly = function () {
                $scope.form.friendsOnly = false;
            };

            function isValidForm() {
                $scope.errors = [];
                if (!MySession.isAuthenticated()) {
                    $scope.errors.push("You must login to create games");
                }
                if (!$scope.form.name || $scope.form.name.trim().length == 0) {
                    $scope.errors.push("Game Title is required");
                }
                if (!$scope.form.duration) {
                    $scope.errors.push("Game Duration is required");
                }
                return $scope.errors.length == 0;
            }

        }]);

})();