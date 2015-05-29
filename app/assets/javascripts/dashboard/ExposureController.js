(function () {
    var app = angular.module('shocktrade');

    /**
     * Exposure Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('ExposureController', ['$scope', '$http', '$log', '$timeout', 'toaster', 'MySession',
        function ($scope, $http, $log, $timeout, toaster, MySession) {

            $scope.chartData = [];
            var colors = ["#00ff00", "#88ffff", "#8888ff", "#ff8000", "#88ffaa", "#ff88ff", "#ff8888"];

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
            $scope.exposure = $scope.exposures[$scope.exposures.length - 1];

            /**
             * Initializes the view by displaying an initial chart
             */
            $scope.init = function () {
                if(MySession.getUserID()) {
                    $scope.exposurePieChart(MySession.getContest(), $scope.exposure.value, MySession.getUserID());
                }
                else {
                    $timeout(function() {
                        $scope.init();
                    }, 1000);
                }
            };

            $scope.exposurePieChart = function (contest, exposure, userID) {
                $http.get("/api/charts/exposure/" + exposure + "/" + contest.OID() + "/" + userID)
                    .success(function (data) {
                        $scope.chartData = data;
                    })
                    .error(function (err) {
                        $log.error("Failed to load " + exposure.label + " data")
                    });
            };

            $scope.colorFunction = function() {
                return function(d, i) {
                    return colors[i % colors.length];
                };
            };

            $scope.xFunction = function(){
                return function(d) {
                    return d.label;
                };
            };

            $scope.yFunction = function(){
                return function(d) {
                    return d.value;
                };
            };

        }]);

})();