(function () {
    var app = angular.module('shocktrade');

    /**
     * Discover Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('DiscoverController', ['$scope', '$cookieStore', '$interval', '$log', '$routeParams', '$timeout', 'toaster', 'FavoriteSymbols', 'HeldSecurities', 'MarketStatus', 'MySession', 'NewOrderDialog', 'QuoteService', 'RecentSymbols',
        function ($scope, $cookieStore, $interval, $log, $routeParams, $timeout, toaster, FavoriteSymbols, HeldSecurities, MarketStatus, MySession, NewOrderDialog, QuoteService, RecentSymbols) {

            // setup a private loading variable
            $scope.loading = false;

            // setup the public variables
            $scope.marketClock = (new Date()).toTimeString();
            $scope.ticker = null;
            $scope.q = {active: true};

            // define the display options
            $scope.options = {
                range: $cookieStore.get("chart_range") || "5d"
            };

            // setup the chart range
            $scope.$watch("options.range", function (newValue, oldValue) {
                $cookieStore.put("chart_range", newValue);
            });

            // setup filtered quotes & trading history
            $scope.filterQuotes = [];
            $scope.tradingHistory = null;
            $scope.selectedTradingHistory = null;

            // define the Quote module expanders
            $scope.expanders = [{
                title: "Performance & Risk",
                url: "/assets/views/discover/quotes/expanders/price_performance.htm",
                icon: "fa-line-chart",
                expanded: false,
                visible: function (q) {
                    return q.high52Week || q.low52Week || q.change52Week || q.movingAverage50Day ||
                        q.movingAverage200Day || q.change52WeekSNP500 || q.beta;
                }
            }, {
                title: "Income Statement",
                url: "/assets/views/discover/quotes/expanders/income_statement.htm",
                icon: "fa-money",
                expanded: false,
                visible: function (q) {
                    return q.revenue || q.revenuePerShare || q.revenueGrowthQuarterly || q.grossProfit ||
                        q.EBITDA || q.netIncomeAvailToCommon || q.dilutedEPS || q.earningsGrowthQuarterly;
                }
            }, {
                title: "Balance Sheet",
                url: "/assets/views/discover/quotes/expanders/balanace_sheet.htm",
                icon: "fa-calculator",
                expanded: false,
                visible: function (q) {
                    return q.totalCash || q.totalDebt || q.currentRatio || q.totalCashPerShare ||
                        q.totalDebtOverEquity || q.bookValuePerShare || q.returnOnAssets ||
                        q.profitMargin || q.mostRecentQuarterDate || q.returnOnEquity ||
                        q.operatingMargin || q.fiscalYearEndDate;
                }
            }, {
                title: "Valuation Measures",
                url: "/assets/views/discover/quotes/expanders/valuation_measures.htm",
                icon: "fa-gears",
                expanded: false,
                visible: function (q) {
                    return q.enterpriseValue || q.trailingPE || q.forwardPE || q.pegRatio || q.priceOverSales ||
                        q.priceOverBookValue || q.enterpriseValueOverRevenue || q.enterpriseValueOverEBITDA ||
                        q.operatingCashFlow || q.leveredFreeCashFlow;
                }
            }, {
                title: "Share Statistics",
                url: "/assets/views/discover/quotes/expanders/share_statistics.htm",
                icon: "fa-bar-chart",
                expanded: false,
                visible: function (q) {
                    return q.avgVolume3Month || q.avgVolume10Day || q.sharesOutstanding || q.sharesFloat ||
                        q.pctHeldByInsiders || q.pctHeldByInstitutions || q.sharesShort || q.shortRatio ||
                        q.shortPctOfFloat || q.sharesShortPriorMonth;
                }
            }, {
                title: "Dividends & Splits",
                url: "/assets/views/discover/quotes/expanders/dividends_splits.htm",
                icon: "fa-cut",
                expanded: false,
                visible: function (q) {
                    return q.forwardAnnualDividendRate || q.forwardAnnualDividendYield ||
                        q.trailingAnnualDividendYield || q.divYield5YearAvg || q.payoutRatio ||
                        q.dividendDate || q.exDividendDate || q.lastSplitFactor || q.lastSplitDate;
                }
            }, {
                title: "Historical Quotes",
                url: "/assets/views/discover/quotes/trading_history.htm",
                icon: "fa-calendar",
                expanded: false,
                loading: false,
                onExpand: function (callback) {
                    if ($scope.tradingHistory === null && $scope.q.assetType === 'Common Stock') {
                        $scope.loadTradingHistory($scope.q.symbol, callback);
                    }
                }
            }];

            $scope.autoCompleteSymbols = function (searchTerm) {
                return QuoteService.autoCompleteSymbols(searchTerm, 20)
                    .then(function (response) {
                        return response.data;
                    });
            };

            /**
             * Initializes the module
             */
            $scope.init = function () {
                // setup market status w/updates
                $interval(function () {
                    $scope.marketClock = (new Date()).toTimeString();
                }, 1000);

                // setup the market status updates
                setupMarketStatusUpdates();
            };

            $scope.popupNewOrderDialog = function (symbol) {
                NewOrderDialog.popup({symbol: symbol});
            };

            $scope.expandSection = function (module) {
                module.expanded = !module.expanded;
                if (module.expanded && module.onExpand) {
                    module.loading = true;
                    var promise = $timeout(function () {
                        module.loading = false;
                    }, 3000);
                    module.onExpand(function () {
                        module.loading = false;
                        $timeout.cancel(promise);
                    });
                }
            };

            $scope.addFavoriteSymbol = function (symbol) {
                FavoriteSymbols.add(symbol);
            };

            $scope.isFavorite = function (symbol) {
                return FavoriteSymbols.isFavorite(symbol);
            };

            $scope.removeFavoriteSymbol = function (symbol) {
                FavoriteSymbols.remove(symbol);
            };

            $scope.hasHoldings = function (q) {
                return q && q.products && (q.legalType === 'ETF') && (q.products.length !== 0);
            };

            $scope.removeRecentSymbol = function (symbol) {
                RecentSymbols.remove(symbol);
            };

            $scope.rowClass = function (column, row) {
                return ( column === "symbol" ) ? row['exchange'] : column;
            };

            $scope.columnAlign = function (column) {
                return ( column === "symbol" ) ? "left" : "right";
            };

            $scope.isOpen = function (filter) {
                return (filter.name === "Favorites");
            };

            $scope.getAssetIcon = function (quote) {
                var q = quote || $scope.q;
                if (q && q.assetType) {
                    switch (q.assetType) {
                        case 'Crypto-Currency' :
                            return "/assets/images/asset_types/bitcoin.png";
                        case 'Currency':
                            return "/assets/images/asset_types/currency.png";
                        case 'ETF':
                            return "/assets/images/asset_types/etf.png";
                        default:
                            return "/assets/images/asset_types/stock.png";
                    }
                }
                else return "/assets/images/status/transparent.png";
            };

            $scope.getMatchedAssetIcon = function (q) {
                //console.log("q = " + angular.toJson(q));
                return "/assets/images/asset_types/stock.png";
            };

            $scope.getRiskClass = function (riskLevel) {
                return riskLevel ? "risk_" + riskLevel.toLowerCase() : null;
            };

            $scope.getRiskDescription = function (riskLevel) {
                if (riskLevel === "Low") return "Generally recommended for investment";
                else if (riskLevel === "Medium") return "Not recommended for inexperienced investors";
                else if (riskLevel === "High") return "Not recommended for investment";
                else if (riskLevel === "Unknown") return "The risk level could not be determined";
                else return "The risk level could not be determined";
            };

            $scope.getBetaClass = function (beta) {
                if (beta == null) return "";
                else if (beta > 1.3 || beta < -1.3) return "volatile_red";
                else if (beta >= 0.0) return "volatile_green";
                else if (beta < 0) return "volatile_yellow";
                else return "";
            };

            $scope.loadTickerQuote = function (_ticker) {
                var ticker = $("#stockTicker").val() || _ticker;
                $scope.loadQuote(ticker);
            };

            $scope.loadQuote = function (ticker) {
                $log.info("Loading symbol " + angular.toJson(ticker));

                // setup the loading animation
                $scope.startLoading();

                // determine the symbol
                var symbol = null;
                if (ticker.symbol) {
                    symbol = ticker.symbol.toUpperCase();
                }
                else {
                    var index = ticker.indexOf(' ');
                    symbol = (index == -1 ? ticker : ticker.substring(0, index)).toUpperCase();
                }

                // load the quote
                QuoteService.loadStockQuote(symbol).then(
                    function (response) {
                        var quote = response.data;
                        if (quote) {
                            // capture the quote
                            $scope.q = quote;
                            $scope.ticker = quote.symbol + " - " + quote.name;

                            // save the cookie
                            $cookieStore.put('symbol', quote.symbol);

                            // add the symbol to the Recently-viewed Symbols
                            RecentSymbols.add(symbol);

                            // get the risk level
                            QuoteService.getRiskLevel(symbol).then(
                                function (response) {
                                    quote.riskLevel = response.data;
                                },
                                function (response) {
                                    toaster.pop('error', 'Error!', "Error retrieving risk level for " + symbol);
                                });

                            // load the trading history
                            $scope.tradingHistory = null;
                            if ($scope.expanders[6].expanded) {
                                $scope.expandSection($scope.expanders[6]);
                            }
                        }
                        else {
                            toaster.pop('error', 'Error!', "No quote found for " + symbol);
                            console.log("Empty quote? " + angular.toJson(quote));
                        }

                        // disabling the loading status
                        $scope.stopLoading();
                    },
                    function (response) {
                        $log.error("Failed to retrieve quote: " + response.status);
                        $scope.stopLoading();
                        toaster.pop('error', 'Error!', "Error loading quote " + symbol);
                    });
            };

            $scope.loadTradingHistory = function (symbol, callback) {
                QuoteService.getTradingHistory(symbol).then(
                    function (results) {
                        $scope.tradingHistory = results;
                        if (callback) callback();
                    },
                    function (response) {
                        toaster.pop('error', 'Error!', "Error loading trading history for " + symbol);
                        if (callback) callback();
                    });
            };

            $scope.loadFilterQuotes = function (filter, index) {
                $scope.loading = true;
                QuoteService.getFilterQuotes(filter).then(
                    function (quotes) {
                        $scope.loading = false;
                        filter.rows = quotes;
                    },
                    function (err) {
                        $scope.loading = false;
                        toaster.pop('error', 'Error!', "Error loading filter " + filter.name);
                    });
            };

            $scope.tradingActive = function (time) {
                return (new Date()).getTime();
            };

            $scope.selectTradingHistory = function(t) {
                $scope.selectedTradingHistory = t;
            };

            $scope.hasSelectedTradingHistory = function() {
                return $scope.selectedTradingHistory != null;
            };

            $scope.isSelectedTradingHistory = function(t) {
                return $scope.selectedTradingHistory === t;
            };

            // maintain the appropriate aspect ratio for the application
            var lastWindowUpdate = 0;
            var mainWidthPctValue = '55%';
            $scope.mainWidthPct = function () {
                var now = (new Date()).getTime();
                if (now - lastWindowUpdate >= 1000) {
                    lastWindowUpdate = now;
                    var width = $('body').innerWidth() - 20;
                    var adjWidth = ((1.0 - 459.0 / width) * 100.0).toFixed(0);
                    mainWidthPctValue = adjWidth + "%";
                    //console.log("pct = " + pct + ", adjWidth = " + adjWidth);
                }
                return mainWidthPctValue;
            };

            // watch for changes to the player's profile
            $scope.$watch("MySession.userProfile", function () {
                if (!MySession.userProfile.favorites) MySession.userProfile.favorites = ['AAPL'];
                if (!MySession.userProfile.recentSymbols) MySession.userProfile.recentSymbols = ['AAPL', 'AMZN', 'GOOG', 'MSFT'];

                // load the favorite and recent quotes
                FavoriteSymbols.setSymbols(MySession.userProfile.favorites);
                RecentSymbols.setSymbols(MySession.userProfile.recentSymbols);

                // setup the filters
                var filters = MySession.userProfile.filters;
                $scope.filterQuotes = [];
                for (var n = 0; n < filters.length; n++) {
                    var clonedFilter = $scope.clone(filters[n]);
                    clonedFilter.rows = [];
                    clonedFilter.maxResults = 10;
                    $scope.filterQuotes.push(clonedFilter);
                }

                // load the held securities
                var id = MySession.userProfile.OID();
                if (id) {
                    HeldSecurities.init(id);
                }
            });

            function setupMarketStatusUpdates() {
                $scope.usMarketsOpen = null;
                $log.info("Retrieving market status...");
                MarketStatus.getMarketStatus(function (response) {
                    // retrieve the delay in milliseconds from the server
                    var delay = response.delay;
                    if (delay < 0) {
                        delay = response.end - response.sysTime;
                        if (delay <= 300000) {
                            delay = 300000; // 5 minutes
                        }
                    }

                    // set the market status
                    $log.info("US Markets are " + (response.active ? 'Open' : 'Closed') + "; Waiting for " + delay + " msec until next trading start...");
                    setTimeout(function () {
                        $scope.usMarketsOpen = response.active;
                    }, 750);

                    // wait for the delay, then call recursively
                    setTimeout(function () {
                        setupMarketStatusUpdates();
                    }, delay);
                });
            }

            ///////////////////////////////////////////////////////////////////////////
            //          Initialization
            ///////////////////////////////////////////////////////////////////////////

            // load the symbol
            (function () {
                if (!$scope.q.symbol) {
                    // get the symbol
                    var symbol = $routeParams.symbol || $cookieStore.get('symbol') || RecentSymbols.getLast();

                    // load the symbol
                    $scope.loadQuote(symbol);
                }
            })();

        }]);

})();