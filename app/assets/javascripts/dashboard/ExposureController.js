(function () {
    var app = angular.module('shocktrade');

    /**
     * Exposure Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('ExposureController', ['$scope', '$http', '$log', '$timeout', 'toaster', 'GraphService',
        function ($scope, $http, $log, $timeout, toaster, GraphService) {

            var cache = {};

            // public variables
            $scope.exposures = [{
                "value": "sector", "label": "Sector Exposure"
            }, {
                "value": "industry", "label": "Industry Exposure"
            }, {
                "value": "exchange", "label": "Exchange Exposure"
            }, {
                "value": "market", "label": "Exchange Sub-Market Exposure"
            }, {
                "value": "securities", "label": "Securities Exposure"
            }];
            $scope.exposure = $scope.exposures[1];
            var lastExposure = null;

            $scope.exposurePieChart = function (contest, exposure, userID, elemId) {
                // define the mapping key
                var key = exposure + "/" + contest.OID();

                // graph a pie chart
                if (!cache[key]) {
                    cache[key] = true;

                    lastExposure = exposure;
                    $http.get("/api/charts/exposure/" + key + "/" + userID)
                        .success(function (data) {
                            GraphService.pieChart(800, 400, data, elemId);
                        })
                        .error(function (err) {
                            $log.error("Failed to load exposure data")
                        });
                }
            };

        }]);

})();