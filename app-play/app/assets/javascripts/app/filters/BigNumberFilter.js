(function() {
    var app = angular.module('shocktrade');

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

})();