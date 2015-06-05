(function() {
    var app = angular.module('shocktrade');

    /**
     * Quote Change Filter
     */
    app.filter('quoteChange', function () {
        function abs(value) {
            return !value ? value : (value < 0 ? -value : value);
        }

        return function (value) {
            if (value == null) return "";
            var num = abs(parseFloat(value));
            if (num >= 100) return num.toFixed(0);
            else if (num >= 10) return num.toFixed(1);
            else return num.toFixed(2);
        };
    });

})();