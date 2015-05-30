(function() {
    var app = angular.module('shocktrade');

    /**
     * Yes/No Boolean Conversion filter
     */
    app.filter('yesno', function() {
        return function (value) {
            return value ? 'Yes' : 'No';
        };
    });

})();