(function () {
    var app = angular.module('shocktrade');

    /**
     * Profile Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('ProfileController', ['$scope', '$log', function ($scope, $log) {
        $scope.profileTabs = [{
            "name": "Share",
            "path": "/assets/views/connect/connect.htm",
            "icon": "fa fa-facebook-square"
        },{
            "name": "My Awards",
            "path": "/assets/views/profile/awards.htm",
            "icon": "fa fa-trophy"
        }, {
            "name": "My Statistics",
            "path": "/assets/views/profile/statistics.htm",
            "icon": "fa fa-bar-chart"
        }];

        $scope.initProfile = function () {

        };

    }]);

})();