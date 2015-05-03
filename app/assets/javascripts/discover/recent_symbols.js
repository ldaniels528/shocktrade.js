/**
 * Recent Symbols Service
 * @author lawrence.daniels@gmail.com
 */
angular
	.module('shocktrade')
	.factory('RecentSymbols', function($rootScope, $http, $log, $q, MySession) {
		
	var service = {
		symbols: [],
		quotes : []
	};
	
	service.getLast = function() {
		var symbols = service.symbols;
		return (symbols.length > 0) ? symbols[symbols.length-1] : "AMZN";
	};

	service.add = function(symbol) {
		var index = indexOf(symbol);
		if( index === -1) {
			// get the user ID
			var id = MySession.getUserID();
			
			// add the symbol to the profile
			if(id != null) {
				$http({ method:'PUT', url:'/api/profile/' + id + '/recent/' + symbol }).then(function(response) {
					service.lastSymbol = symbol;
					service.symbols.unshift(symbol);
					loadQuotes(service.symbols);
				});
			}
		}
	};
	
	service.isEmpty = function() {
		return !service.symbols.length;
	};

	service.remove = function(symbol) {
		var index = indexOf(symbol);
		if( index != -1) {
			// get the user ID
			var id = MySession.getUserID();
			
			// remove the symbol from the profile
			$http({ method:'DELETE', url:'/api/profile/' + id + '/recent/' + symbol }).then(function(response) {
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
	}

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
			for(var n = 0; n < quotes.length; n++) {
				quotes[n].favorite = $rootScope.FavoriteSymbols.isFavorite(quotes[n].symbol);
				quotes[n].held = $rootScope.HeldSecurities.isHeld(quotes[n].symbol);
			}
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