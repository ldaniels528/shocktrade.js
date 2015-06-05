(function () {
    var app = angular.module('shocktrade');

    /**
     * Explore: Drill-Down Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('DrillDownController', ['$scope', '$anchorScroll', '$cookieStore', '$http', '$location', '$log', '$routeParams', '$timeout', 'QuoteService', 'RecentSymbols',
        function ($scope, $anchorScroll, $cookieStore, $http, $location, $log, $routeParams, $timeout, QuoteService, RecentSymbols) {

            // tree data
            $scope.sectors = [];
            $scope.refreshTree = function () {
                QuoteService.loadSectors().then(function (response) {
                    $scope.sectors = [];
                    angular.forEach(response.data, function (v) {
                        $scope.sectors.push({
                            label: v._id,
                            total: v.total
                        });
                    });

                    // expand the sector, industry, sub-industry for the current symbol
                    $timeout(function () {
                        var symbol = $scope.selectedSymbol();
                        $scope.expandSectorForSymbol(symbol);
                    }, 500);
                });
            };

            $scope.selectedSymbol = function () {
                return $routeParams.symbol || $cookieStore.get('symbol') || RecentSymbols.getLast();
            };

            $scope.expandOrCollapseSector = function (sector, callback) {
                if (!sector.expanded) {
                    sector.loading = true;
                    QuoteService.loadIndustries(sector.label).then(
                        function (response) {
                            sector.loading = false;
                            sector.industries = [];
                            angular.forEach(response.data, function (v) {
                                sector.industries.push({
                                    label: v._id,
                                    total: v.total
                                });
                            });
                            sector.expanded = true;

                            // invoke the callback
                            if (callback) {
                                callback(sector, sector.industries);
                            }
                        },
                        function (response) {
                            sector.loading = false;
                        });
                }
                else {
                    sector.expanded = false;
                }
            };

            $scope.expandOrCollapseIndustry = function (sector, industry, callback) {
                if (!industry.expanded) {
                    industry.loading = true;
                    QuoteService.loadSubIndustries(sector.label, industry.label).then(
                        function (response) {
                            industry.loading = false;
                            industry.subIndustries = [];
                            angular.forEach(response.data, function (v) {
                                industry.subIndustries.push({
                                    label: v._id,
                                    total: v.total
                                });
                            });
                            industry.expanded = true;

                            // invoke the callback
                            if (callback) {
                                callback(sector, industry, industry.subIndustries);
                            }
                        },
                        function (response) {
                            industry.loading = false;
                        });
                }
                else {
                    industry.expanded = false;
                }
            };

            $scope.expandOrCollapseSubIndustry = function (sector, industry, subIndustry, callback) {
                if (subIndustry && !subIndustry.expanded) {
                    subIndustry.loading = true;
                    QuoteService.loadIndustryQuotes(sector.label, industry.label, subIndustry ? subIndustry.label : null).then(
                        function (response) {
                            subIndustry.loading = false;
                            subIndustry.quotes = response.data;
                            subIndustry.expanded = true;

                            // invoke the callback
                            if (callback) {
                                callback(sector, industry, subIndustry, subIndustry.quotes);
                            }
                        },
                        function (response) {
                            subIndustry.loading = false;
                        });
                }
                else {
                    if(subIndustry) subIndustry.expanded = false;
                }
            };

            $scope.expandSectorForSymbol = function (symbol) {
                // lookup the symbol's sector information
                QuoteService.loadSectorInfo(symbol).then(function (response) {
                    // get the sector information
                    var info = response.data[0];

                    // find the symbol (expand: sector >> industry >> sub-industry >> symbol)
                    $log.info("Expanding " + info.sector + "....");
                    $scope.expandOrCollapseSector(findLabel($scope.sectors, info.sector), function (sector, industries) {
                        $log.info("Expanding " + info.sector + " >> " + info.industry + "....");
                        $scope.expandOrCollapseIndustry(sector, findLabel(industries, info.industry), function (sector, industry, subIndustries) {
                            $log.info("Expanding " + info.sector + " >> " + info.industry + " >> " + info.subIndustry + "....");
                            $scope.expandOrCollapseSubIndustry(sector, industry, findLabel(subIndustries, info.subIndustry, function () {
                                $location.hash(10000);
                                $anchorScroll();
                            }));
                        });
                    });
                });
            };

            function findLabel(array, label) {
                for (var n = 0; n < array.length; n++) {
                    if (array[n].label == label) return array[n];
                }
                return null;
            }

        }]);

})();