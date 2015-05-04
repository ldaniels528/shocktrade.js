(function () {
    var app = angular.module('shocktrade');

    /**
     * Escape filter
     */
    app.filter('escape', function () {
        return window.escape;
    });

    /**
     * Big Number Filter
     */
    app.filter('bigNumber', function () {
        return function (value) {
            if (value == null || value == "") return "";
            var temp = value.replace(/\,/g, '');
            var num = parseFloat(temp);

            // negative #'s
            if (num <= -1.0e+12)
                return (num / 1.0e+12).toFixed(2) + "T";
            else if (num <= -1.0e+9)
                return (num / 1.0e+9).toFixed(2) + "B";
            else if (num <= -1.0e+6)
                return (num / 1.0e+6).toFixed(2) + "M";
            else if (num <= -1.0e+3)
                return (num / 1.0e+3).toFixed(2) + "K";

            // positive #'s
            else if (num >= 1.0e+12)
                return (num / 1.0e+12).toFixed(2) + "T";
            else if (num >= 1.0e+9)
                return (num / 1.0e+9).toFixed(2) + "B";
            else if (num >= 1.0e+6)
                return (num / 1.0e+6).toFixed(2) + "M";
            else if (num <= 1.0e+3)
                return (num / 1.0e+3).toFixed(2) + "K";

            // default
            else return value;
        };
    });

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