(function () {
    var app = angular.module('shocktrade');

    /**
     * Exposure Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('ExposureController', ['$scope', '$http', '$log', '$timeout', 'toaster', 'GraphService', 'MySession',
        function ($scope, $http, $log, $timeout, toaster, GraphService, MySession) {
            $scope.exposure = "industry";
            var lastExposure = null;

            $scope.exposureBySector = function (contest, exposure, userID, elemId) {
                // graph a pie chart
                if (exposure !== lastExposure) {
                    lastExposure = exposure;
                    $http.get("/api/charts/exposure/" + exposure + "/" + contest.OID() + "/" + userID)
                        .success(function (data) {
                            GraphService.pieChart(400, 400, 200, data, elemId);
                        })
                        .error(function (err) {
                            $log.error("Failed to load exposure data")
                        });
                }
            };

        }]);

})();