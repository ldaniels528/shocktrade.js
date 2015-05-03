/**
 * Messaging Controller
 * @author lawrence.daniels@gmail.com
 */
angular
	.module('shocktrade')
	.controller('MessagingCtrl', ['$scope', '$modalInstance', function($scope, $modalInstance) {
	
	$scope.items = ['item1', 'item2', 'item3']; // TODO testing only
	$scope.selected = {
		item: $scope.items[0]
	};

	$scope.ok = function () {
		$modalInstance.close($scope.selected.item);
	};

	$scope.cancel = function () {
		$modalInstance.dismiss('cancel');
	};
	
}]);

