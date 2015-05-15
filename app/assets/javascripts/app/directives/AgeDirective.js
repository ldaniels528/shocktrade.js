(function() {
    var app = angular.module('shocktrade');

    /**
     * Age Directive
     * @author lawrence.daniels@gmail.com
     * <age value="{{ myDate }}" /> ~> "5 days ago"
     */
    app.directive('age', ['$log', function($log) {
        return {
            restrict: 'E',
            scope: { class:'@class', value:'@value' },
            transclude: true,
            replace: false,
            template: "<span class='{{ class }}'>{{ age }} ago</span>",
            link: function(scope, element, attrs) {
                var units = [ "min", "hour", "day", "month", "year" ];
                scope.$watch( "value", function() {
                    var age = Math.abs((new Date()).getTime() - scope.value) / 60000;
                    var unit = 0;
                    if(age >= 60) { age /= 60; unit++; } // minutes -> hours
                    if(age >= 24) { age /= 24; unit++; } // hours -> days
                    if(age >= 30) { age /= 30; unit++; } // days -> months
                    if(age >= 12) { age /= 12; unit++; } // months -> years
                    age = age.toFixed(0);
                    scope.age = age + " " + units[unit] + ( age != 1 ? "s" : "" );
                });
            }
        };
    }]);

})();