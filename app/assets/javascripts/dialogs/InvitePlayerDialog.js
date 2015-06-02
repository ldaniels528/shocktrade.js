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
    app.factory('InvitePlayerDialog', function ($http, $log, $modal, Facebook, MySession) {
        var service = {};

        /**
         * Invite a player via pop-up dialog
         */
        service.popup = function (contest) {

            var modalInstance = $modal.open({
                templateUrl: 'invite_player_dialog.htm',
                controller: 'PlayerInviteCtrl',
                resolve: {
                    myFriends: function () {
                        return MySession.fbFriends;
                    }
                }
            });

            modalInstance.result.then(function (selectedFriends) {
                if(selectedFriends.length) {
                    $log.info("selectedFriends = " + angular.toJson(selectedFriends));
                    Facebook.send("http://www.nytimes.com/interactive/2015/04/15/travel/europe-favorite-streets.html");
                }

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
    app.controller('PlayerInviteCtrl', ['$scope', '$log', '$modalInstance', 'MySession', 'myFriends',
        function ($scope, $log, $modalInstance, MySession, myFriends) {

            $scope.invites = [];

            $scope.getInvitedCount = function () {
                var count = 0;
                angular.forEach($scope.invites, function (invitee) {
                    if (invitee) count += 1;
                });
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
                    if (invites[n]) selectedFriends.push(myFriends[n]);
                }
                return selectedFriends;
            }

        }]);

})();
