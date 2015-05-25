(function () {
    var app = angular.module('shocktrade');

    /**
     * News Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('NewsController', ['$scope', '$cookieStore', '$log', '$sce', '$timeout', 'MySession', 'NewsQuoteDialog', 'NewsService', 'NewsSymbols',
        function ($scope, $cookieStore, $log, $sce, $timeout, MySession, NewsQuoteDialog, NewsService, NewsSymbols) {

            $scope.channels = [];
            $scope.selection = {};
            $scope.newsSources = [];
            $scope.view = $cookieStore.get('NewsController_view') || 'list';

            ///////////////////////////////////////////////////////////////////////////
            //          Public Functions
            ///////////////////////////////////////////////////////////////////////////

            $scope.getNewsFeed = function (feedId) {
                $scope.startLoading();
                NewsService.getFeed(feedId).then(
                    function (response) {
                        populateQuotes(response.data);
                        $scope.channels = enrichTickers(response.data);
                        $scope.stopLoading();
                    },
                    function (err) {
                        $scope.stopLoading();
                    });
            };

            /**
             * Return the appropriate class to create a diagonal grid
             */
            $scope.gridClass = function (index) {
                var row = Math.floor(index / 2);
                var cell = (row % 2 == 0) ? index % 2 : (index + 1) % 2;
                return "news_tile" + cell;
            };

            /**
             * Displays a quote in a pop-up dialog
             */
            $scope.loadNewsQuote = function (symbol) {
                NewsQuoteDialog.popup({symbol: symbol});
            };

            $scope.loadNewsSources = function () {
                $scope.startLoading();
                NewsService.getSources()
                    .success(function (newsSources) {
                        $scope.newsSources = newsSources;
                        $scope.selection.feed = newsSources[0].OID();
                        $scope.getNewsFeed($scope.selection.feed);
                        $scope.stopLoading();
                    })
                    .error(function (err) {
                        $scope.stopLoading();
                    });
            };

            $scope.trustMe = function (html) {
                return $sce.trustAsHtml(html);
            };

            ///////////////////////////////////////////////////////////////////////////
            //          Private Functions
            ///////////////////////////////////////////////////////////////////////////

            function enrichTickers(channels) {
                for (var n = 0; n < channels.length; n++) {
                    var items = channels[n].items;
                    for (var m = 0; m < items.length; m++) {
                        var item = items[m];
                        var quotes = item.quotes;
                        if (quotes.length) {
                            item.description = replaceSymbols(item.description, quotes);
                        }

                        // add ... to the end of incomplete sentences
                        if (item.description.charAt(item.description.length - 1) != '.') {
                            item.description += ' ...';
                        }
                    }
                }
                return channels;
            }

            function replaceSymbols(description, quotes) {
                for (var n = 0; n < quotes.length; n++) {
                    var q = quotes[n];
                    var term = '( ' + q.symbol + ' )';
                    var start = description.indexOf(term);
                    if (start != -1) {
                        var end = start + term.length;
                        return replace(description, start, end,
                            '(<a href="#/discover/' + q.symbol + '"><span ' + popup(q) + ' class="' + q.exchange + '">' +
                            q.symbol +
                            '</span></a>' + changeArrow(q) + ')');
                    }
                }
                return description;
            }

            function popup(q) {
                return 'popover-title="' + q.name + ' (' + q.exchange + ')" ' +
                    'popover="' + q.sector + ' &#8212; ' + q.industry + '" ' +
                    'popover-trigger="mouseenter" popover-placement="right" ';
            }

            function populateQuotes(channels) {
                // gather the quotes
                var quotes = [];
                for (var n = 0; n < channels.length; n++) {
                    var items = channels[n].items;
                    for (var m = 0; m < items.length; m++) {
                        angular.forEach(items[m].quotes, function (q, index) {
                            quotes.push(q);
                        });
                    }
                }

                // set the quotes
                NewsSymbols.setQuotes(quotes);
            }

            function changeArrow(q) {
                var isNeg = (q.changePct < 0);
                var icon = isNeg ? "fa fa-arrow-down" : "fa fa-arrow-up";
                var num = q.changePct;
                return "<span class='" + icon + (isNeg ? " negative" : " positive" ) + "'>" + num + "%</span>";
            }

            function replace(source, start, end, replacement) {
                var s1 = source.substring(0, start);
                var s2 = source.substring(end, source.length);
                return s1 + replacement + s2;
            }

        }]);
})();