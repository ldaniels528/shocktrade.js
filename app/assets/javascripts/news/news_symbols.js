(function () {
    var app = angular.module('shocktrade');

    /**
     * News Symbols Service
     * @author lawrence.daniels@gmail.com
     */
    app.factory('NewsSymbols', function ($http, $log, $q, HeldSecurities, FavoriteSymbols) {
        var service = {
            quotes: []
        };

        service.isEmpty = function () {
            return service.quotes.length === 0;
        };

        service.setQuotes = function (quotes) {
            service.quotes = quotes;
        };

        service.getQuotes = function () {
            angular.forEach(service.quotes, function(quote) {
                quote.favorite = FavoriteSymbols.isFavorite(quote.symbol);
                quote.held = HeldSecurities.isHeld(quote.symbol);
            });
            return service.quotes;
        };

        return service;
    });

})();