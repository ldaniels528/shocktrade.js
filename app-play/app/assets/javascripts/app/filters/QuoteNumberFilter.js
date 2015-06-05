(function() {
    var app = angular.module('shocktrade');

    /**
     * Quote Number Filter
     */
    app.filter('quoteNumber', function () {
        return function (value) {
            if (value == null) return "";
            var num = parseFloat(value);
            if (num <= -10000) return num.toFixed(0);
            else if (num <= -100) return num.toFixed(2);
            else if (num <= -10) return num.toFixed(2);
            else if (num <= 0) return num.toFixed(4);
            else if (num < 0.0001) return num.toFixed(5);
            else if (num >= 10000) return num.toFixed(2);
            else if (num >= 100) return num.toFixed(2);
            else if (num >= 10) return num.toFixed(3);
            else if (num >= 1) return num.toFixed(4);
            else return num.toFixed(4);
        };
    });

})();