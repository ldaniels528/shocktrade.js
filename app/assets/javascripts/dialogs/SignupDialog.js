(function () {
    // get the application reference
    var app = angular.module('shocktrade');

    /**
     * Sign-Up Service
     * @author lawrence.daniels@gmail.com
     */
    app.factory('SignUpDialog', function ($http, $log, $modal, MySession) {
        var service = {};

        /**
         * Sign-up Modal Dialog
         */
        service.popup = function (fbUserID, fbProfile) {
            // create an instance of the dialog
            var $modalInstance = $modal.open({
                controller: 'SignUpController',
                templateUrl: 'sign_up.htm',
                resolve: {
                    params: function () {
                        return {
                            "fbUserID": fbUserID,
                            "fbProfile": fbProfile
                        }
                    }
                }
            });

            $modalInstance.result.then(function (profile) {
                MySession.authenticated = true;
                MySession.userProfile = profile;

            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };

        service.createAccount = function (form) {
            $log.info("Creating account " + angular.toJson(form));
            return $http({method: "POST", url: "/api/profile/create", data: angular.toJson(form)});
        };

        return service;
    });

    /**
     * Sign-Up Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('SignUpController', ['$scope', '$log', '$modalInstance', '$timeout', 'toaster', 'params', 'SignUpDialog',
        function ($scope, $log, $modalInstance, $timeout, toaster, params, SignUpDialog) {
            $scope.form = {
                name: params.fbProfile.name,
                facebookID: params.fbUserID,
                email: null
            };
            $scope.loading = false;
            $scope.params = params;
            $scope.messages = [];

            $scope.ok = function () {
                if(isValid($scope.form)) {
                    $scope.loading = true;
                    SignUpDialog.createAccount($scope.form)
                        .success(function (profile) {
                            stopLoading();
                            if (!profile.error) {
                                $modalInstance.close(profile);
                            }
                            else {
                                $scope.messages.push(profile.error);
                            }
                        })
                        .error(function (xhr, status, error) {
                            stopLoading();
                            $log.error("Error: " + error.status);
                            toaster.pop('error', 'Error!', xhr.error);
                        });
                }
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

            function stopLoading() {
                $timeout(function() {
                    $scope.loading = false;
                }, 1000);
            }

            /**
             * Validates the form
             * @param form the given form
             * @returns {boolean}
             */
            function isValid(form) {
                // clear messages
                $scope.messages = [];

                // validate the user name
                if(!form.userName || form.userName.trim().length === 0) {
                    $scope.messages.push("Screen Name is required");
                }

                // validate the email address
                if(!form.email || form.email.trim().length === 0) {
                    $scope.messages.push("Email Address is required");
                }

                if(form.email && !form.email.isValidEmail()) {
                    $scope.messages.push("The Email Address format is invalid");
                }

                // it's valid is the messages are empty
                return $scope.messages.length === 0;
            }

        }]);

})();