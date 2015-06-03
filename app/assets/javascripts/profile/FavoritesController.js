(function () {
    var app = angular.module('shocktrade');

    /**
     * Favorites Controller
     * @author lawrence.daniels@gmail.com
     */
    app.controller('FavoritesController', ['$scope', '$log', '$timeout', 'toaster', 'FavoriteSymbols',
        function ($scope, $log, $timeout, toaster, FavoriteSymbols) {
            $scope.selectedQuote = null;

            $scope.initFavorites = function () {

            };

            $scope.cancelSelection = function () {
                $scope.selectedQuote = null;
            };

            $scope.isSplitScreen = function () {
                return $scope.selectedQuote !== null;
            };

            $scope.selectQuote = function (quote) {
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

        }]);

})();