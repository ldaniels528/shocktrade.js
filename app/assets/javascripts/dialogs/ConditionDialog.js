(function () {
    var app = angular.module('shocktrade');

    /**
     * Condition Dialog Singleton
     * @author lawrence.daniels@gmail.com
     */
    app.factory('ConditionDialog', function ($http, $log, $modal) {
        var service = {};

        /**
         * Condition pop-up dialog
         */
        service.popup = function ($scope) {

            var modalInstance = $modal.open({
                templateUrl: 'condition_dialog.htm',
                controller: 'ConditionDialogCtrl',
                resolve: {
                    filter: function () {
                        return $scope.filter
                    }
                }
            });

            modalInstance.result.then(
                function (filter) {
                    $scope.filter = filter
                },
                function (err) {
                    $log.info('Modal dismissed at: ' + new Date())
                })
        };

        return service;
    });

    /**
     * Condition Dialog Pop-pop Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('ConditionDialogCtrl', ['$scope', '$log', '$modalInstance', 'MySession',
        function ($scope, $log, $modalInstance, MySession) {

            $scope.selection = {};

            $scope.queryFields = [
                {id: "BETA", name: "Beta"},
                {id: "CHANGE", name: "Change"},
                {id: "LAST_TRADE", name: "Last"},
                {id: "OPEN", name: "Open"},
                {id: "CLOSE", name: "Close"},
                {id: "HIGH", name: "High"},
                {id: "LOW", name: "Low"},
                {id: "SPREAD", name: "Spread"},
                {id: "VOLUME", name: "Volume"}
            ];

            $scope.operators = ["=", ">=", ">", "<", "<="];

            $scope.addCondition = function (filter) {
                // create the condition
                var condition = {
                    field: $scope.selection.field,
                    operator: $scope.selection.operator,
                    value: parseFloat($scope.selection.value)
                };

                // add it to the filter
                filter.conditions.push(condition);
                $scope.selection = {};
            };

            $scope.getFilters = function () {
                return MySession.userProfile.filters;
            };

            $scope.getSelectedFilter = function () {
                var scope = angular.element($("#search_main")).scope();
                return scope.filter;
            };

            $scope.ok = function () {
                var selectedFilter = $scope.getSelectedFilter();
                $modalInstance.close(selectedFilter);
            };

            $scope.cancel = function () {
                $scope.selection = {};
                $modalInstance.dismiss('cancel');
            };

        }]);

})();