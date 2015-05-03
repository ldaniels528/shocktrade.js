/**
 * News Quote Form Pop-pop Controller
 * @author lawrence.daniels@gmail.com
 */
angular
	.module('shocktrade')
	.controller('NewsQuoteCtrl', ['$scope', '$log', '$modalInstance', 'ContestService', 'QuoteService', 'symbol',
	                      function($scope, $log, $modalInstance, ContestService, QuoteService, symbol) {
	
	$scope.quote = {};
	
	$scope.init = function(symbol) {
		var scope = angular.element($("#NewsBlock")).scope();
		ContestService.orderQuote(symbol).then(
			function(data) {
				$scope.quote = data;
			},
			function(response) {
				$log.error("Error: " + response.status);
			});
	};
	
	$scope.ok = function() {
		$modalInstance.close('Ok');
	};

	$scope.cancel = function() {
		$modalInstance.dismiss('cancel');
	};	

}]);
