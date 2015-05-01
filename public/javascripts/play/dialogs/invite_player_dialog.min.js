/**
 * Invite Player Dialog Service and Controller
 * @author lawrence.daniels@gmail.com
 */
(function () {
    var app = angular.module('shocktrade');

    /**
     * Invite Player Dialog Singleton
     * @author lawrence.daniels@gmail.com
     */
    app.factory('InvitePlayerDialog', function ($http, $log, $modal, $q) {
        var service = {};

        /**
         * Invite a player via pop-up dialog
         */
        service.popup = function ($scope, contest) {

            var modalInstance = $modal.open({
                templateUrl: 'invite_player_dialog.htm',
                controller: 'PlayerInviteCtrl',
                resolve: {
                    invites: function () {
                        return $scope.selectedFriends;
                    }
                }
            });

            modalInstance.result.then(function (selectedFriends) {
                $log.info("selectedFriends = " + angular.toJson(selectedFriends));
            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };

        return service;
    });

    /**
     * Player Invite Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('PlayerInviteCtrl', ['$scope', '$log', '$modalInstance', 'MySession',
        function ($scope, $log, $modalInstance, MySession) {

            $scope.invites = [];

            $scope.getInvitedCount = function () {
                var count = 0;
                var invites = $scope.invites;
                for (var n = 0; n < invites.length; n++) {
                    if (invites[n]) count += 1;
                }
                return count;
            };

            $scope.ok = function () {
                $modalInstance.close(getSelectedFriends($scope.invites));
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

            function getSelectedFriends(invites) {
                var selectedFriends = [];
                for (var n = 0; n < invites.length; n++) {
                    if (invites[n]) selectedFriends.push(MySession.fbFriends[n]);
                }
                return selectedFriends;
            }

        }]);

})();
