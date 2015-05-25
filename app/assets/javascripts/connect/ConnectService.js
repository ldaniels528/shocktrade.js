(function () {
    var app = angular.module('shocktrade');

    /**
     * Connect Service
     * @author lawrence.daniels@gmail.com
     */
    app.factory('ConnectService', function ($http, $q) {
        var service = {};

        service.deleteMessages = function (messageIDs) {
            var deferred = $q.defer();
            $http({
                method: 'DELETE',
                url: '/api/updates',
                data: angular.toJson(messageIDs)
            }).success(function (data, status, headers, config) {
                deferred.resolve(data[0]);
            }).error(function (response) {
                deferred.reject(response);
            });
            return deferred.promise;
        };

        service.getUserInfo = function (fbUserId) {
            var deferred = $q.defer();
            $http.get('/api/profile/facebook/' + fbUserId)
                .success(function (data, status, headers, config) {
                    deferred.resolve(data[0]);
                })
                .error(function (data, status, headers, config) {
                    deferred.reject("Error: " + symbol + "-" + data + "(" + status + ")");
                });
            return deferred.promise;
        };

        service.getUserUpdates = function (userName, limit) {
            return $http.get('/api/updates/' + userName + '/' + limit);
        };

        service.identifyFacebookFriends = function (fbFriends) {
            // build the JSON query list
            var list = [];
            for (var n = 0; n < fbFriends.length; n++) {
                list.push(fbFriends[n].id);
            }

            // make the service call
            var deferred = $q.defer();
            $http({
                method: 'POST',
                url: '/api/profile/facebook/friends',
                data: angular.toJson(fbFriends)
            }).success(function (data, status, headers, config) {
                deferred.resolve(data);
            }).error(function (data, status, headers, config) {
                deferred.reject("Error: " + symbol + "-" + data + "(" + status + ")");
            });
            return deferred.promise;
        };

        return service;
    });

})();