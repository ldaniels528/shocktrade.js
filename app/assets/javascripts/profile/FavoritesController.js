(function () {
    var app = angular.module('shocktrade');

    /**
     * Favorites Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('FavoritesController', ['$scope', '$location', '$log', '$routeParams', '$timeout', 'toaster', 'FavoriteSymbols',
        function ($scope, $location, $log, $routeParams, $timeout, toaster, FavoriteSymbols) {
            $scope.selectedQuote = null;

            $scope.cancelSelection = function () {
                $scope.selectedQuote = null;
            };

            $scope.isSplitScreen = function () {
                return $scope.selectedQuote !== null;
            };

            $scope.selectQuote = function (quote) {
                $location.search('symbol', quote.symbol);
                $scope.selectedQuote = quote;
            };

            /////////////////////////////////////////////////////////////////////////////
            //			C.R.U.D. Functions
            /////////////////////////////////////////////////////////////////////////////

            $scope.getFavoriteQuotes = function () {
                return FavoriteSymbols.getQuotes();
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

            /////////////////////////////////////////////////////////////////////////////
            //			Initialization
            /////////////////////////////////////////////////////////////////////////////

            (function() {
                var symbol = $routeParams.symbol;
                if(symbol) {
                    angular.forEach($scope.getFavoriteQuotes(), function(quote) {
                        if(quote.symbol === symbol) {
                            $scope.selectQuote(quote);
                        }
                    });
                }
            })();

        }]);

})();