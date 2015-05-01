/**
 * Favorite Symbols Service
 * @author lawrence.daniels@gmail.com
 */
angular
	.module('shocktrade')
	.factory('FavoriteSymbols', function($rootScope, $http, $log, $q) {
		
	var service = {
		symbols: [],
		quotes : []
	};

	service.isEmpty = function() {
		return !service.symbols.length;
	};
	
	service.isFavorite = function(symbol) {
		return indexOf(symbol) != -1;
	};

	service.add = function(symbol) {
		var index = indexOf(symbol);
		if( index == -1) {
			// get the user ID
			var id = $rootScope.MySession.getUserID();
			
			// add the symbol to the profile's Favorites
			$http({ method:'PUT', url:'/api/profile/' + id + '/favorite/' + symbol }).then(function(response) {
				service.symbols.unshift(symbol);
				loadQuotes(service.symbols);
			});
		}
	};
	
	service.remove = function(symbol) {
		var index = indexOf(symbol);
		if( index != -1) {
			// get the user ID
			var id = MySession.getUserID();
			
			// remove the symbol from the profile's Favorites			
			$http({ method:'DELETE', url:'/api/profile/' + id + '/favorite/' + symbol }).then(function(response) {
				service.symbols.splice(index, 1);
				loadQuotes(service.symbols);
			});
		}
	};
	
	service.setSymbols = function(symbols) {
		service.symbols = symbols;
		loadQuotes(symbols);
	};

	service.getQuotes = function() {
		return service.quotes;
	};

	service.updateQuote = function(quote) {
		for(var n = 0; n < service.quotes.length; n++) {
			if(service.quotes[n].symbol == quote.symbol) {
				service.quotes[n] = quote;
				return;
			}
		}
	};

	function indexOf(symbol) {
		for(var n = 0; n < service.symbols.length; n++) {
			if( symbol == service.symbols[n]) {
				return n;
			}
		}
		return -1;
	};
	
	function loadQuotes(symbols) {
		return $http({
			method : 'POST',
			url : '/api/quotes/list',
			data : angular.toJson(symbols)
		}).then(function(response) {
			var quotes = response.data;
			service.quotes = quotes;
			return quotes;
		});
	};

	/**
	 * Listen for quote updates
	 */
	$rootScope.$on("quote_updated", function(event, quote) {
		service.updateQuote(quote);
	});
	
	return service;
});