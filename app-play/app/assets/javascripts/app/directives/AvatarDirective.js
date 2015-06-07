(function() {
    var app = angular.module('shocktrade');

    /**
     * Avatar Directive
     * @author lawrence.daniels@gmail.com
     * <avatar id="{{ p.facebookID }}" class="avatar-24"/>
     */
    app.directive('avatar', ['$log', function($log) {
        return {
            restrict: 'E',
            scope: { id:'@id', object:'@object', alt:'@alt', class:'@class', style:'@style' },
            transclude: true,
            replace: false,
            template: '<img ng-src="{{ url }}" ng-class="myClass" class="{{ class }}" style="{{ style }}">',
            link: function(scope, element, attrs) {
                function setURL(scope) {
                    if (scope.id) {
                        scope.url = "http://graph.facebook.com/" + scope.id + "/picture";
                        scope.myClass = "playerAvatar";
                    }
                    else if (scope.object) {
                        var json = JSON.parse(scope.object);
                        if(!json) scope.url = scope.alt || "/assets/images/avatars/avatar100.png";
                        else if(json.picture) scope.url = json.picture.data.url;
                        else if(json.facebookID) scope.url = "http://graph.facebook.com/" + json.facebookID + "/picture";
                        else {
                            $log.error("Avatar object type could not be determined - " + scope.object);
                            scope.url = scope.alt || "/assets/images/avatars/avatar100.png";
                        }
                        scope.myClass = "playerAvatar";
                    }
                    else {
                        scope.url = scope.alt || "/assets/images/avatars/avatar100.png";
                        scope.myClass = "spectatorAvatar";
                    }
                }

                scope.$watch("object", function() {
                    setURL(scope);
                });
                scope.$watch("id", function() {
                    setURL(scope);
                });
            }
        };
    }]);

})();