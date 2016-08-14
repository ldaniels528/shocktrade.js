(function() {
    var app = angular.module('shocktrade');

    /**
     * Country Directive
     * @author lawrence.daniels@gmail.com
     * <country id="{{ p.facebookID }}" style="width: 24px; height: 24px"/>
     */
    app.directive('country', ['$log', function($log) {
        return {
            restrict: 'E',
            scope: { profile:'@profile' },
            transclude: true,
            replace: false,
            template: '<img ng-src="{{ url }}" style="vertical-align: middle" />',
            link: function(scope, element, attrs) {
                scope.url = "/assets/images/country/us.png";
            }
        };
    }]);

})();