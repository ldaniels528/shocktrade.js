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
                // graph a pie chart
                if (!cache[exposure]) {
                    cache[exposure] = true;
                    $log.info("Loading exposure data for " + exposure + " into " + elemId);

                    lastExposure = exposure;
                    $http.get("/api/charts/exposure/" + exposure + "/" + contest.OID() + "/" + userID)
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