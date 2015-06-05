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
            template: '<i ng-class="icon"></i>',
            link: function(scope, element, attrs) {
                scope.$watch( "value", function() {
                    var changeVal = ( scope.value || 0 );
                    scope.icon = changeVal >= 0 ? "fa fa-arrow-up positive" : "fa fa-arrow-down negative";
                });
            }
        };
    }]);

})();