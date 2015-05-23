(function () {
    var app = angular.module('shocktrade');

    /**
     * Search Controller
     */
    app.controller('ResearchController', ['$scope', '$cookieStore', '$http', '$log', '$timeout', 'toaster',
        function ($scope, $cookieStore, $http, $log, $timeout, toaster) {
            $scope.searchOptions = {
                maxResults: 250
            };

            // search reference data components
            $scope.filteredResults = [];
            $scope.searchResults = [];
            $scope.exchangeCounts = {};
            $scope.sortBy = null;
            $scope.reverse = false;

            $scope.maxResultsSet = [25, 50, 100, 250];
            $scope.priceRanges = [0, 1, 5, 10, 15, 20, 25, 30, 40, 50, 75, 100];
            $scope.volumeRanges = [0, 1000, 5000, 10000, 20000, 50000, 100000, 250000, 500000, 1000000, 5000000];
            $scope.percentages = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100];

            // search reference data components
            $scope.exchangeSets = {
                "AMEX": true,
                "NASDAQ": true,
                "NYSE": true,
                "OTCBB": true,
                "OTHER_OTC": true
            };

            $scope.filterExchanges = function () {
                $scope.loading = true;
                $scope.filteredResults = $scope.searchResults.filter(function (q) {
                    return $scope.exchangeSets[q.exchange];
                });

                $timeout(function () {
                    $scope.loading = false;
                }, 1000);
            };

            $scope.getSearchResults = function () {
                return $scope.filteredResults;
            };

            $scope.getSearchResultsCount = function () {
                return $scope.filteredResults.length;
            };

            $scope.quoteSearch = function (searchOptions) {
                $scope.filteredResults = [];
                $scope.searchResults = [];

                // execute the search
                $scope.loading = true;
                $http.post('/api/research/search', searchOptions)
                    .success(function (results) {
                        var exchanges = [];
                        angular.forEach(results, function (q) {
                            // normalize the exchange
                            q.market = q.exchange;
                            q.exchange = normalizeExchange(q.exchange);

                            // count the quotes by exchange
                            if (!exchanges[q.exchange]) exchanges[q.exchange] = 1; else exchanges[q.exchange]++;

                            // add missing exchanges to our set
                            if ($scope.exchangeSets[q.exchange] == undefined) {
                                $scope.exchangeSets[q.exchange] = true;
                            }
                        });

                        // update the exchange counts
                        $scope.exchangeCounts = exchanges;
                        $scope.searchResults = results;
                        $scope.filterExchanges();
                        //$scope.loading = false;

                    }).error(function (data, status, headers, config) {
                        $log.error("Quote Search Failed - json => " + angular.toJson(searchOptions));
                        toaster.pop('error', 'Failed to execute search', null);
                        $scope.loading = false;
                    });
            };

            $scope.getSearchResultClass = function (count) {
                if (!count) return "results_none";
                else if (count < 250) return "results_small";
                else if (count < 350) return "results_medium";
                else return "results_large";
            };

            $scope.rowClass = function (column, row) {
                return ( column === "symbol" ) ? row['exchange'] : column
            };

            $scope.columnAlign = function (column) {
                return ( column === "symbol" ) ? "left" : "right"
            };

            ///////////////////////////////////////////////////////////////////////////
            //          Private Functions
            ///////////////////////////////////////////////////////////////////////////

            function normalizeExchange(market) {
                if (market.indexOf("ASE") == 0) return market;
                else if (market.indexOf("NAS") == 0) return "NASDAQ";
                else if (market.indexOf("NCM") == 0) return "NASDAQ";
                else if (market.indexOf("NGM") == 0) return "NASDAQ";
                else if (market.indexOf("NMS") == 0) return "NASDAQ";
                else if (market.indexOf("NYQ") == 0) return "NYSE";
                else if (market.indexOf("NYS") == 0) return "NYSE";
                else if (market.indexOf("OBB") == 0) return "OTCBB";
                else if (market.indexOf("OTC") == 0) return "OTCBB";
                else if (market.indexOf("OTHER") == 0) return "OTHER_OTC";
                else if (market.indexOf("PCX") == 0) return market;
                else if (market.indexOf("PNK") == 0) return "OTCBB";
                else {
                    $log.warn("exchange = " + market);
                    return market;
                }
            }

        }]);

})();