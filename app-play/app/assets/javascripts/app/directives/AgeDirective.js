(function() {
    var app = angular.module('shocktrade');

    // define the time units and factors
    var units = ["min", "hour", "day", "month", "year"];
    var factors = [60, 24, 30, 12];

    /**
     * Age Directive
     * @author lawrence.daniels@gmail.com
     * <age value="{{ myDate }}" /> ~> "5 days ago"
     */
    app.directive('age', ['$log', function() {
        return {
            restrict: 'E',
            scope: { class:'@class', value:'@value' },
            transclude: true,
            replace: false,
            template: "<span class='{{ class }}'>{{ age }} ago</span>",
            link: function(scope, element, attrs) {
                scope.$watch( "value", function() {
                    var age = Math.abs((new Date()).getTime() - scope.value) / 60000;

                    var unit = 0;
                    while (age >= factors[unit]) {
                        age /= factors[unit];
                        unit++;
                    }
                    age = age.toFixed(0);
                    scope.age = age + " " + units[unit] + ( age != 1 ? "s" : "" );
                });
            }
        };
    }]);

})();