var app = angular.module('shocktrade');

app.controller('BlogCtrl', ['$scope', 'BlogService', function($scope, BlogService) {
	$scope.posts = [];
	$scope.posts = {};
	$scope.loading = false;
	$scope.message = '';
	$scope.readLimit = 50;
	
	$scope.createBlogPost = function(post) {
		$scope.startLoading();
		BlogService.createBlogPost(post).then(
			function(data) {
				$scope.posts = {};
				$scope.stopLoading();
				$scope.getBlogPosts($scope.readLimit);				
			},
			function(err) {
				$scope.message = 'Failed to create the posting';
				$scope.stopLoading();
				console.log("Error: Blog -" + data + " (" + status + ")");
			});		
	};	
	
	$scope.updateBlogPost = function(post) {
		$scope.startLoading();
		BlogService.updateBlogPost(post).then(
			function(data) {
				$scope.posts = {};
				$scope.stopLoading();
				$scope.getBlogPosts($scope.readLimit);				
			},
			function(err) {
				$scope.message = 'Failed to update the posting';
				$scope.stopLoading();
				console.log("Error: Blog -" + data + " (" + status + ")");				
			});		
	};		
	
	$scope.getBlogPost = function(id) {
		$scope.startLoading();
		BlogService.getBlogPost(id).then(
			function(data) {
				$scope.stopLoading();
				$scope.post = data;
			},
			function(err) {
				$scope.message = 'Failed to retrieve the posting';
				$scope.stopLoading();
				console.log("Error: Blog -" + data + " (" + status + ")");
			});		
	};
	
	$scope.getBlogPosts = function(count) {
		$scope.startLoading();
		BlogService.getBlogPosts(count).then(
			function(data) {
				$scope.stopLoading();
				$scope.posts = data;
			},
			function(err) {
				$scope.message = 'Failed to retrieve the postings';
				$scope.stopLoading();
				console.log("Error: Blog -" + data + " (" + status + ")");
			});		
	};
	
	$scope.removeBlogPost = function(id) {
		$scope.startLoading();
		BlogService.removeBlogPost(id).then(
			function(data) {
				$scope.stopLoading();
				$scope.getBlogPosts($scope.readLimit);
			},
			function(err) {
				$scope.message = 'Failed to remove the posting';
				$scope.stopLoading();
				console.log("Error: Blog -" + data + " (" + status + ")");
			});
	};
	
}]);