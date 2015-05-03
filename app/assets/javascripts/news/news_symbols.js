/**
 * News Symbols Service
 * @author lawrence.daniels@gmail.com
 */
angular
	.module('shocktrade')
	.factory('NewsSymbols', function($http, $log, $q, HeldSecurities, FavoriteSymbols, MySession) {
		
	var service = {
		quotes : []
	};

	service.isEmpty = function() {
		return service.quotes.length === 0;
	};
	
	service.setQuotes = function(quotes) {
		service.quotes = quotes;
	};
	
	service.getQuotes = function() {
		var quotes = service.quotes;
		for(var n = 0; n < quotes.length; n++) {
			quotes[n].favorite = FavoriteSymbols.isFavorite(quotes[n].symbol);
			quotes[n].held = HeldSecurities.isHeld(quotes[n].symbol);
		}
		return quotes;
	};
	
	return service;
});