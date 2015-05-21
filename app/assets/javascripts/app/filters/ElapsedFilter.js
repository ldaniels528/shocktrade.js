(function() {
    var app = angular.module('shocktrade');

    /**
     * Elapsed Time filter
     */
    app.filter('elapsed', function() {
        return function (time) {
            return toDuration(time);
        };
    });

})();