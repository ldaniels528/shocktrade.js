(function () {
    var app = angular.module('shocktrade');

    /**
     * Search Controller
     */
    app.controller('ResearchController', ['$scope', '$cookieStore', '$http', '$log', '$timeout', 'ConditionDialog', 'Errors', 'HeldSecurities', 'MySession', 'QuoteService',
        function ($scope, $cookieStore, $http, $log, $timeout, ConditionDialog, Errors, HeldSecurities, MySession, QuoteService) {
            $scope.message = "";

            // search reference data components
            $scope.filteredSearchResults = [];
            $scope.searchResults = [];
            $scope.counts = [];
            $scope.count = 0;
            $scope.sortBy = null;
            $scope.reverse = false;

            // define the current filter
            $scope.filter = {
                sortField: "CHANGE"
            };

            // define the filter quotes
            $scope.filterQuotes = [];

            // define the current selection
            $scope.selection = {
                filterId: $cookieStore.get('filterId'),
                maxResults: "25"
            };

            // search reference data components
            $scope.exchangeSets = [
                {id: "AMEX", selected: true},
                {id: "NASDAQ", selected: true},
                {id: "NYSE", selected: true},
                {id: "OTCBB", selected: true},
                {id: "OTHER_OTC", selected: true}
            ];

            $scope.exchangeMapping = [];
            for (var n = 0; n < $scope.exchangeSets.length; n++) {
                var e = $scope.exchangeSets[n];
                $scope.exchangeMapping[e.id] = $scope.exchangeSets[n]
            }

            $scope.maxResultsSet = [25, 50, 100, 250];
            $scope.searchFields = [
                {id: "SYMBOL", name: "Symbol"}, {id: "EXCHANGE", name: "Exchange"},
                {id: "CHANGE", name: "Change"}, {id: "LAST_TRADE", name: "Last"},
                {id: "OPEN", name: "Open"}, {id: "CLOSE", name: "Close"},
                {id: "SPREAD", name: "Spread"}, {id: "VOLUME", name: "Volume"}
            ];

            $scope.filterSetup = function () {
                var filters = MySession.userProfile.filters;
                if (filters && (filters.length > 0)) {
                    // set the active filter
                    $scope.filter = filters[0];
                    $scope.selection.filterId = filters[0].OID();
                    $scope.search();

                    // build the filter list
                    $scope.filterQuotes = [];
                    for (var n = 0; n < filters.length; n++) {
                        var clonedFilter = clone(filters[n]);
                        clonedFilter.rows = [];
                        clonedFilter.maxResults = 10;
                        $scope.filterQuotes.push(clonedFilter)
                    }
                }
            };

            $scope.loadFilterQuotes = function (filter, index) {
                filter.loading = true;
                QuoteService.getFilterQuotes(filter).then(
                    function (quotes) {
                        filter.rows = quotes;
                        $timeout(function () {
                            filter.loading = false
                        }, 750)
                    },
                    function (err) {
                        Errors.addMessage("Error loading filter " + filter.name);
                        $timeout(function () {
                            filter.loading = false
                        }, 750)
                    })
            };

            $scope.search = function () {
                $scope.message = "";
                $scope.loading = true;

                var filter = $scope.filter;
                filter.maxResults = parseInt($scope.selection.maxResults);

                // execute the search
                QuoteService.filterSearch(filter)
                    .then(
                    function (results) {
                        updateSearchResults(results);
                        $scope.loading = false
                    },
                    function (data, status, headers, config) {
                        $log.error("Filter Match - json => " + angular.toJson(filter));
                        Errors.addMessage("Failed to execute filter search");
                        $scope.loading = false
                    })
            };

            function updateCounts(results) {
                try {
                    var counts = [];
                    for (var n = 0; n < results.length; n++) {
                        var exchg = results[n].exchange;
                        if (exchg) {
                            if (!counts[exchg]) counts[exchg] = 1;
                            else counts[exchg]++
                        }
                    }
                    $scope.counts = counts;
                }
                catch (err) {
                    $log.error(angular.toJson(err))
                }
            }

            function updateFilteredSearchResults() {
                $scope.filteredSearchResults = $scope.searchResults.filter(function (q) {
                    if (!q) return false;
                    else {
                        var e = $scope.exchangeMapping[q.exchange];
                        return !e || e.selected
                    }
                });
                $scope.count = $scope.filteredSearchResults.length
            }

            function updateSearchResults(results) {
                $scope.searchResults = results;
                updateCounts(results);
                updateFilteredSearchResults();
            }

            $scope.getSearchResultClass = function (count) {
                if (!count || count == 0) return "results_none";
                else if (count < 250) return "results_small";
                else if (count < 350) return "results_medium";
                else return "results_large";
            };

            $scope.ascendingDescending = function () {
                $scope.filter.ascending = !$scope.filter.ascending;
                $scope.search();
            };

            $scope.rowClass = function (column, row) {
                return ( column === "symbol" ) ? row['exchange'] : column
            };

            $scope.columnAlign = function (column) {
                return ( column === "symbol" ) ? "left" : "right"
            };

            $scope.updateFilter = function (filterId) {
                console.log("filterId = " + angular.toJson(filterId, null, '\t'));

                var filters = MySession.userProfile.filters;
                if (filters) {
                    for (var n = 0; n < filters.length; n++) {
                        if (filters[n].OID() == filterId) {
                            $scope.filter = filters[n];
                            console.log("filter = " + angular.toJson(filters[n], null, '\t'));
                            $scope.search();
                            return
                        }
                    }
                }
            };

            /**
             * Opens a new Condition Pop-up Dialog
             */
            $scope.conditionDialogPopup = function () {
                ConditionDialog.popup($scope);
            };

            $scope.$on('exchange_updated', function (event, message) {
                console.log("State of exchange '" + message + "' has changed")
                $scope.search()
            });

            // watch exchanges
            $scope.$watch("exchangeSets", function () {
                updateFilteredSearchResults()
            }, true);

            // watch for changes to the player's profile
            $scope.$watch("MySession.userProfile", function () {
                $scope.filterSetup()
            })

        }]);

})();