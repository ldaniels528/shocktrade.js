(function() {
    var app = angular.module('shocktrade');

    /**
     * Stock Change Arrow Directive
     * @author lawrence.daniels@gmail.com
     * <changeArrow value="{{ q.change }}" />
     */
    app.directive('changearrow', ['$log', function($log) {
        return {
            restrict: 'E',
            scope: { value:'@value' },
            transclude: true,
            replace: false,
            template: '<img ng-src="{{ icon }}" class="middle" />',
            link: function(scope, element, attrs) {
                scope.$watch( "value", function() {
                    var changeVal = ( scope.value || 0 );
                    scope.icon = "/assets/images/status/" + (changeVal >= 0 ? "stock_up.gif" : "stock_down.gif");
                });
            }
        };
    }]);

})();