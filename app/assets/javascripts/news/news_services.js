(function () {
    var app = angular.module('shocktrade');

    /**
     * News Service
     * @author lawrence.daniels@gmail.com
     */
    app.factory('NewsService', function ($http, $q) {
        var cache = {};
        var service = {};

        service.loadFeed = function (feedId) {
            return $http.get('/api/news/feed/' + feedId);
        };

        service.getFeed = function (feedId) {
            if (cache[feedId] != null) return cache[feedId];
            else {
                var channels = service.loadFeed(feedId);
                cache[feedId] = channels;
                return channels;
            }
        };

        service.getSources = function () {
            return $http.get('/api/news/sources');
        };

        return service;
    });

})();