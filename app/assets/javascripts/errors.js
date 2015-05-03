/**
 * Error Message Service
 * @author lawrence.daniels@gmail.com
 */
angular.module('shocktrade').factory('Errors', function($timeout) {
	var service = { };
	service.errorMessages = [];

	/**
	 * Clears the error messages
	 */
	service.clearMessages = function() {
		service.errorMessages = [];
	}
	
	/**
	 * Adds an error message
	 */
	service.addMessage = function(message) {
		service.errorMessages.push(message);
		
		$timeout(function() {
			service.errorMessages = [];
		}, 5000);
	}
	
	/**
	 * Retrieves all error messages
	 */
	service.getMessages = function() {
		return service.errorMessages;
	}
	
	service.hasErrors = function() {
		return service.errorMessages.length;
	}
	
	return service;
});