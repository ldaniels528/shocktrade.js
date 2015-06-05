(function () {
    // get the application reference
    var app = angular.module('shocktrade');

    /**
     * Compose Message Dialog Singleton
     * @author lawrence.daniels@gmail.com
     */
    app.factory('ComposeMessageDialog', function ($http, $log, $modal, $q) {
        var service = {};

        /**
         * Popups the Compose Message Dialog
         */
        service.popup = function (params) {
            // create an instance of the dialog
            var $modalInstance = $modal.open({
                templateUrl: 'compose_message.htm',
                controller: 'ComposeMessageDialogCtrl',
                resolve: {
                    params: function () {
                        return params;
                    }
                }
            });

            $modalInstance.result.then(function (selectedItem) {
                $scope.selected = selectedItem;
            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };

        return service;
    });

    /**
     * Compose Message Dialog Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('ComposeMessageDialogCtrl', ['$scope', '$modalInstance', function ($scope, $modalInstance) {
        $scope.form = {};

        $scope.ok = function () {
            $modalInstance.close($scope.form);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

})();