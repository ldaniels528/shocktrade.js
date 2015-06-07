(function () {
    var app = angular.module('shocktrade');

    /**
     * Preferences Controller
     */
    app.controller('PreferencesController', ['$scope', '$cookieStore', '$http', '$log', '$timeout', 'toaster', 'MySession', 'ProfileService', 'QuoteService',
        function ($scope, $cookieStore, $http, $log, $timeout, toaster, MySession, ProfileService, QuoteService) {

            // exchange loading variable
            $scope.updatingExchanges = false;

            // search reference data components
            $scope.exchanges = {
                AMEX: {id: "AMEX", desc: "New York", selected: true, count: null},
                NASDAQ: {id: "NASDAQ", desc: "New York", selected: true, count: null},
                NYSE: {id: "NYSE", desc: "New York", selected: true, count: null},
                OTCBB: {id: "OTCBB", desc: "New York", selected: true, count: null},
                OTHER_OTC: {id: "OTHER_OTC", desc: "New York", selected: true, count: null}
            };

            /**
             * Refreshes the exchange counts (and selections if authorized)
             */
            $scope.refreshCounts = function () {
                // load the exchange counts
                QuoteService.getExchangeCounts().then(function (response) {
                    // create the exchange to count mapping
                    var exchanges = {};
                    angular.forEach(response.data, function (v, k) {
                        exchanges[v._id] = v.total
                    });

                    // populate the counts
                    angular.forEach($scope.exchanges, function (xchg, idx) {
                        $scope.exchanges[idx].count = exchanges[xchg.id]
                    })
                });

                // set the exchange selection preferences
                if (MySession.isAuthorized()) {
                    var id = MySession.getUserID();

                    // load the user's exchange preferences
                    ProfileService.getExchanges(id).then(function (response) {
                        var results = response.data[0];

                        // build the exchange mapping
                        var mapping = {};
                        angular.forEach($scope.exchanges, function (v, k) {
                            mapping[k] = false
                        });

                        angular.forEach(results.exchanges, function (v, k) {
                            mapping[v] = true
                        });
                        console.log("mapping => " + angular.toJson(mapping));

                        // turn on/off each exchange
                        angular.forEach(mapping, function (v, k) {
                            $scope.exchanges[k].selected = v
                        })
                    })
                }
            };

            $scope.setExchangeState = function (exchange) {
                if (MySession.isAuthorized()) {
                    var id = MySession.getUserID();
                    QuoteService.setExchangeState(id, exchange.id, !exchange.selected).then(function () {
                        // request search results update
                        $scope.$emit('exchange_updated', exchange.id);
                    })
                }
            };

            /**
             * Persists update exchange preferences
             */
            $scope.updateExchanges = function () {
                $scope.updatingExchanges = true;

                // build the array
                var exchanges = [];
                angular.forEach($scope.exchanges, function (xchg, idx) {
                    if (xchg.selected) exchanges.push(xchg.id)
                });

                // perform the update
                var id = MySession.getUserID();
                ProfileService.updateExchanges(id, exchanges).then(
                    function (response) {
                        $scope.updatingExchanges = false
                    },
                    function (response) {
                        $scope.updatingExchanges = false
                    })
            };

            // watch for changes to the player's profile
            $scope.$watch(MySession.getUserProfile(), function () {
                $scope.refreshCounts()
            })

        }]);

})();
