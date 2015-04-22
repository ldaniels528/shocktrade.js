@(appId: String)

var app = angular.module('shocktrade');
app.factory('Facebook', function($q) {		
    var service = {
    	"userID" : null,
    	"auth" : null,
    	"accessToken": null
    };
      
    service.getFriends = function() {
    	var deferred = $q.defer();
    	FB.api("/me/friends?access_token=" + service.accessToken,
    		function (response) {
		      if (response && !response.error) {
		    	  deferred.resolve(response);
		      }
		      else {
		    	  deferred.reject(response);
		      }
    	});
    	return deferred.promise;
    };

    service.getLoginStatus = function() {
    	var deferred = $q.defer();
    	FB.getLoginStatus(function(response) {
    		if (response.status === 'connected') {
    			// the user is logged in and has authenticated your app, and response.authResponse supplies
    			// the user's ID, a valid access token, a signed request, and the time the access token
    			// and signed request each expire
    			service.userID = response.authResponse.userID;
    			service.accessToken = response.authResponse.accessToken;
    			service.auth = response.authResponse;
    			deferred.resolve(response);
    		} else if (response.status === 'not_authorized') {
    			// the user is logged in to Facebook, but has not authenticated your app
    			deferred.reject(response);
    		} else {
    			// the user isn't logged in to Facebook.
    			deferred.reject(response);
    		}			
    	});  	 
    	return deferred.promise;
    };
      
    service.login = function() {	
    	var deferred = $q.defer();
    	FB.login(function(response) {
    		if (response.authResponse) {
    			service.auth = response.authResponse;
    			service.userID = response.authResponse.userID;
    			service.accessToken = response.authResponse.accessToken;
    			deferred.resolve(response);
    		} else {
    			deferred.reject(response);
    		}
        });
    	return deferred.promise;
    };

    service.logout = function() {	
    	  var deferred = $q.defer();
    	  FB.logout(function(response) {
    		  if (response) {
    			  service.auth = null;
    			  deferred.resolve(response);
    		  } else {
    			  deferred.reject(response);
    		  }		
    	  });
    	  return deferred.promise;
      };
      
      service.getUserProfile = function() {
    	  var deferred = $q.defer();
    	  FB.api("/me?access_token=" + service.auth.accessToken,
    		function (response) {
		      if (response && !response.error) {
		    	  deferred.resolve(response);
		      }
		      else {
		    	  deferred.reject(response);
		      }
		  });  
    	  return deferred.promise;
      };
    
    return service;
});

// Facebook SDK injector
(function(d) {
	// is the element our script?
    var id = 'facebook-jssdk';
    if (d.getElementById(id)) { return; }
    
    // dynamically create the script
    var js = d.createElement('script'); 
    js.id = id; 
    js.async = true;
    js.src = "http://connect.facebook.net/en_US/all.js";
    
    // get the script and insert our dynamic script
    var ref = d.getElementsByTagName('script')[0];
    ref.parentNode.insertBefore(js, ref);
}(document));

// Asynchronously load the Facebook SDK 
window.fbAsyncInit = function() {
	// initialize the Facebook SDK
	FB.init({
		appId: '@appId',
		status: true,
		xfbml: true
	});
	
	// get the login status
	FB.getLoginStatus(function(response) {
		if (response.status === 'connected') {	    	
			// capture the Facebook login status
			if( response.authResponse ) {
				// capture the user ID and access token
		    	var rootElem = $("#ShockTradeMain");
		    	var injector = angular.element(rootElem).injector();
				var Facebook = injector.get("Facebook");
				if(Facebook) {
					Facebook.auth = response.authResponse;
					Facebook.userID = response.authResponse.userID;
					Facebook.accessToken = response.authResponse.accessToken;
				}
				else {
					console.log("Facebook service could not be retrieved");
				}
				
				// react the the login status
		    	var scope = angular.element(rootElem).scope();
		    	if(scope) {
					scope.$apply(function() {	
						scope.facebookLoginStatus(Facebook.userID);
					});
		    	}
		    	else {
					console.log("scope could not be retrieved");
				}
			}
		}
    });
};
