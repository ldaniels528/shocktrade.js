(function() {
    var app = angular.module('shocktrade');

    /**
     * Time-based Duration filter
     */
    app.filter('duration', function() {
        return function (time) {
            return toDuration(time);
        };
    });

})();