(function() {
    var app = angular.module('shocktrade');

    /**
     * Elapsed Time filter
     */
    app.filter('elapsed', function() {
        var units = ["sec", "min", "hour", "day", "month", "year" ];
        return function (time) {
            if(time == null) return null;
            var age = Math.abs((new Date()).getTime() - time) / 1000;
            var unit = 0;
            if(age >= 60) { age /= 60; unit++; } // seconds -> minutes
            if(age >= 60) { age /= 60; unit++; } // minutes -> hours
            if(age >= 24) { age /= 24; unit++; } // hours -> days
            if(age >= 30) { age /= 30; unit++; } // days -> months
            if(age >= 12) { age /= 12; unit++; } // months -> years
            age = age.toFixed(0);
            return age + " " + units[unit] + ( age != 1 ? "s" : "" ) + " ago";
        };
    });

})();