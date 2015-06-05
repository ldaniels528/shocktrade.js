(function() {
    var app = angular.module('shocktrade');

    /**
     * Escape filter
     */
    app.filter('escape', function () {
        return window.escape;
    });

})();