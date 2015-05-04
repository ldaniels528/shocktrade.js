(function () {
    var app = angular.module('shocktrade');

    /**
     * Rewards Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('RewardsCtrl', ['$scope', function ($scope) {
        var tabClasses;
        $scope.tabs = [{
            "name": "Awards",
            "path": "/assets/views/play/awards.htm",
            "imageURL": "/assets/images/objects/award.gif",
            "active": false
        }, {
            "name": "Perks",
            "path": "/assets/views/play/perks.htm",
            "imageURL": "/assets/images/objects/gift.png",
            "active": false
        }, {
            "name": "Statistics",
            "path": "/assets/views/play/statistics.htm",
            "imageURL": "/assets/images/objects/stats.gif",
            "active": false
        }];

        //set the initial URL
        $scope.enhanceURL = $scope.tabs[0].path;

        // define the levels
        $scope.levels = [
            {"number": 1, "nextLevelXP": 1000, "description": "Private"},
            {"number": 2, "nextLevelXP": 2000, "description": "Private 1st Class"},
            {"number": 3, "nextLevelXP": 4000, "description": "Corporal"},
            {"number": 4, "nextLevelXP": 8000, "description": "First Corporal"},
            {"number": 5, "nextLevelXP": 16000, "description": "Sergeant"},
            {"number": 6, "nextLevelXP": 32000, "description": "Staff Sergeant"},
            {"number": 7, "nextLevelXP": 64000, "description": "Gunnery Sergeant"},
            {"number": 8, "nextLevelXP": 1280000, "description": "Master Sergeant"},
            {"number": 9, "nextLevelXP": 256000, "description": "First Sergeant"},
            {"number": 10, "nextLevelXP": 1024000, "description": "Sergeant Major"},
            {"number": 11, "nextLevelXP": 2048000, "description": "Warrant Officer 3rd Class"},
            {"number": 12, "nextLevelXP": 4096000, "description": "Warrant Officer 2nd Class"},
            {"number": 13, "nextLevelXP": 4096000, "description": "Warrant Officer 1st Class"},
            {"number": 14, "nextLevelXP": 8192000, "description": "Chief Warrant Officer"},
            {"number": 15, "nextLevelXP": 8192000, "description": "Master Chief Warrant Officer"},
            {"number": 16, "nextLevelXP": 16384000, "description": "Lieutenant"},
            {"number": 17, "nextLevelXP": 32768000, "description": "First Lieutenant"},
            {"number": 18, "nextLevelXP": 65536000, "description": "Captain"},
            {"number": 19, "nextLevelXP": 131072000, "description": "Major"},
            {"number": 20, "nextLevelXP": 262144000, "description": "Lieutenant Colonel"},
            {"number": 21, "nextLevelXP": 524288000, "description": "Colonel"},
            {"number": 22, "nextLevelXP": 524288000, "description": "Brigadier General"},
            {"number": 23, "nextLevelXP": 524288000, "description": "Major General"},
            {"number": 24, "nextLevelXP": 524288000, "description": "Lieutenant General"},
            {"number": 25, "nextLevelXP": 524288000, "description": "General"}];


        function initTabs() {
            tabClasses = ["", "", "", ""];
        }

        $scope.changeTab = function (tabIndex, event) {
            // make all of the tabs inactive
            for (var n = 0; n < $scope.tabs.length; n++) {
                $scope.tabs[n].active = false;
            }

            // set the specific tab
            $scope.tabs[tabIndex].active = true;
            $scope.tab = tabIndex;
            $scope.enhanceURL = $scope.tabs[tabIndex].path;

            // prevent the default action
            if (event) {
                event.preventDefault();
            }
        };

        $scope.getTabClass = function (tabNum) {
            return tabClasses[tabNum];
        };

        $scope.getTabPaneClass = function (tabNum) {
            return "tab-pane " + tabClasses[tabNum];
        };

        $scope.setActiveTab = function (tabNum) {
            initTabs();
            tabClasses[tabNum] = "active";
        };


        //Initialize
        initTabs();
        $scope.setActiveTab(0);

    }]);

})();