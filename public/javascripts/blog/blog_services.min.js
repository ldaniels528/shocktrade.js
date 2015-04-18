var app = angular.module('shocktrade');

app.factory("BlogService", function($http, $q) {
	var service = {};
	
	service.createBlogPost = function(post) {
		var deferred = $q.defer();
		$http({
			method : 'PUT',
			url : '/api/blog',
			data : angular.toJson({
				title: post.title,
				body: post.body,
				creationTime: new Date(),
				author: service.fbProfile.name
			})
		}).success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).error(function(data, status, headers, config) {
			deferred.reject(data, status, headers, config);
		});	
		return deferred.promise;
	};	
	
	service.updateBlogPost = function(post) {
		var deferred = $q.defer();
		$http({
			method : 'POST',
			url : '/api/blog',
			data : angular.toJson(post)
		}).success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).error(function(data, status, headers, config) {
			deferred.reject(data, status, headers, config);
		});	
		return deferred.promise;			
	};		
	
	service.getBlogPost = function(id) {
		var deferred = $q.defer();
		$http({
			method : 'GET',
			url : '/api/blog/id/' + id
		}).success(function(data, status, headers, config) {
			deferred.resolve(data[0]);
		}).error(function(data, status, headers, config) {
			deferred.reject(data, status, headers, config);
		});	
		return deferred.promise;		
	};
	
	service.getBlogPosts = function(count) {
		var deferred = $q.defer();
		$http({
			method : 'GET',
			url : '/api/blog/list/' + count
		}).success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).error(function(data, status, headers, config) {
			deferred.reject(data, status, headers, config);
		});	
		return deferred.promise;			
	};
	
	service.removeBlogPost = function(id) {
		var deferred = $q.defer();
		$http({
			method : 'DELETE',
			url : '/api/blog/' + id
		}).success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).error(function(data, status, headers, config) {
			deferred.reject(data, status, headers, config);
		});	
		return deferred.promise;		
	};	
	
	return service;
});