(function () {
    var app = angular.module('shocktrade');

    /**
     * Connect Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('ConnectController', ['$scope', '$log', 'toaster', 'ComposeMessageDialog', 'ConnectService', 'MySession',
        function ($scope, $log, toaster, ComposeMessageDialog, ConnectService, MySession) {
            $scope.searchName = "";
            $scope.myUpdates = [];
            $scope.myUpdate = null;
            $scope.contact = {};

            /**
             * Selects a specific contact/friend
             */
            $scope.chooseContact = function (friend) {
                //$log.info("contact = " + JSON.stringify(friend, null, '\t'));
                $scope.contact = friend;
                $scope.getUserInfo(friend.id);
            };

            /**
             * Selects the contact in the player's friends list
             */
            $scope.chooseFirstContact = function () {
                var friends = MySession.fbFriends;
                if (friends && friends.length > 0) {
                    $scope.chooseContact(friends[0]);
                }
            };

            /**
             * Retrieve a limited set of user profile information for a specific contact/friend
             */
            $scope.getUserInfo = function (fbUserId) {
                $scope.startLoading();
                ConnectService.getUserInfo(fbUserId).then(
                    function (profile) {
                        $scope.stopLoading();
                        $scope.contact.profile = profile;
                    },
                    function (err) {
                        $scope.stopLoading();
                        console.log("Error: Connect -" + data + " (" + status + ")");
                        toaster.pop('error', 'Failed to retrieve the user profile for contact ' + $scope.contact.name, null);
                    }
                );
            };

            $scope.identifyFacebookFriends = function (fbFriends) {
                $scope.startLoading();
                ConnectService.identifyFacebookFriends(fbFriends).then(
                    function (response) {
                        // TODO add logic here
                        $scope.stopLoading();
                    },
                    function (err) {
                        $scope.stopLoading();
                    });
            };

            /**
             * Returns the contacts matching the given search term
             */
            $scope.getContactList = function (searchTerm) {
                var fbFriends = MySession.fbFriends;
                if (!searchTerm && (searchTerm === "")) return fbFriends.slice(0, 40);
                else {
                    var term = searchTerm.toLowerCase();
                    return fbFriends.filter(function (contact) {
                        return contact.name.toLowerCase().indexOf(term) != -1;
                    });
                }
            };

            /**
             * Loads updates from the database
             */
            $scope.loadMyUpdates = function (userName) {
                if (userName) {
                    $scope.startLoading();

                    ConnectService.getUserUpdates(userName, 50)
                        .success(function (data) {
                            $scope.stopLoading();
                            $scope.myUpdates = data;
                            $scope.myUpdate = null;

                            for (var n = 0; n < data.length; n++) {
                                data[n].selected = false;
                            }
                            $scope.loading = false;
                        })
                        .error(function (err) {
                            $scope.stopLoading();
                            console.log("Error: Connect -" + data + " (" + status + ")");
                            toaster.pop('error', "Failed to load Connect", null);
                            $scope.loading = false;
                        });
                }
            };

            /**
             * Selects a specific message
             */
            $scope.selectUpdate = function (entry) {
                $scope.myUpdate = entry;
            };

            /**
             * Composes a new message via pop-up dialog
             */
            $scope.composeMessage = function () {
                ComposeMessageDialog.popup({
                    "success": function() {
                        $scope.loadMyUpdates(MySession.getUserName());
                    }
                });
            };

            /**
             * Deletes selected messages
             */
            $scope.deleteMessages = function (userName) {
                $scope.startLoading();

                // gather the records to delete
                var myUpdates = $scope.myUpdates;
                var messageIDs = [];
                for (var n = 0; n < myUpdates.length; n++) {
                    if (myUpdates[n].selected) {
                        messageIDs.push(myUpdates[n].OID());
                    }
                }

                // delete the records
                $log.info("messageIDs = " + angular.toJson(messageIDs));
                if (messageIDs.length > 0) {
                    ConnectService.deleteMessages(messageIDs).then(
                        function (response) {
                            $scope.stopLoading();
                            $scope.loadMyUpdates(userName);
                        },
                        function (err) {
                            $scope.stopLoading();
                            toaster.pop('error', 'Failed to delete message', null);
                        }
                    );
                }
                else {
                    $scope.stopLoading();
                    toaster.pop('error', 'No message(s) selected', null);
                }
            };

            /**
             * Selects all currently visible messages
             */
            $scope.selectAll = function (checked) {
                var data = $scope.myUpdates;
                for (var n = 0; n < data.length; n++) {
                    data[n].selected = checked;
                }
            };

            // watch for changes to the player's profile
            $scope.$watch(MySession.getUserProfile(), function () {
                $scope.loadMyUpdates(MySession.getUserName());
                //$scope.chooseFirstContact();
            });

        }]);

})();