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

	/**
	 * Avatar Directive
	 * @author lawrence.daniels@gmail.com
	 * <avatar id="{{ p.fbUserID }}" style="width: 24px; height: 24px"/>
	 */
    app.directive('avatar', ['$log', function($log) {
		return {
		    restrict: 'E',
		    scope: { id:'@id', object:'@object', alt:'@alt', style:'@style' },
		    transclude: true,
		    replace: false,
		    template: '<img ng-src="{{ url }}" ng-class="myClass" style="{{ style }}" {{ extras }}>',
		    link: function(scope, element, attrs) {
                scope.extras = "";

                function setURL(scope) {
                    if (scope.id) {
                        scope.url = "http://graph.facebook.com/" + scope.id + "/picture";
                        scope.myClass = "playerAvatar";
                    }
                    else if (scope.object) {
                        var json = JSON.parse(scope.object);
						if(!json) scope.url = scope.alt || "/assets/images/avatars/avatar100.png";
                        else if(json.picture) scope.url = json.picture.data.url;
                        else if(json.fbUserID) scope.url = "http://graph.facebook.com/" + json.fbUserID + "/picture";
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

	/**
	 * Country Directive
	 * @author lawrence.daniels@gmail.com
	 * <country id="{{ p.fbUserID }}" style="width: 24px; height: 24px"/>
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

	/**
	 * News Directive
	 * @author lawrence.daniels@gmail.com
	 * <news text="{{ myDate }}" /> ~> "5 days ago"
	 */
	app.directive('news', ['$log', function($log) {
		return {
		    restrict: 'E',
		    scope: { class:'@class', content:'@content' },
		    transclude: true,
		    replace: false,
		    template:
		    	'<div class="{{ class }}">' +
					'<a href="" ng-click="loadNewsQuote(\'{{symbol}}\')">' +
						'<nobr><span class="{{exchange}}">{{symbol}} {{changeArrow(change)}</span>' +
						'<span class="{{ changeClass(changePct) }}">{{ changePct }}</span></nobr>' +
					'</a>' +
		    	'</div>',
		    link: function(scope, element, attrs) {
		    	scope.$watch( "content", function() {
		    		var start = scope.content.indexOf("{$", start);
					var end = scope.content.indexOf("$}", start);
					if(start != -1 && end > start) {
						var token = scope.content.substring(start+2, end - 1);
						console.log("token = '" + token + "'");
						var pcs = token.split("|");
						scope.symbol = pcs[0];
						scope.exchange = pcs[1];
						scope.changePct = pcs[2];
						console.log("symbol = " + scope.symbol + ", exchange = " + scope.exchange + ", changePct = " + scope.changePct);
		    		}
		    	});
		    }
		};
	}]);

    app.directive('compile', ['$compile', function ($compile) {
        return function (scope, element, attrs) {
            scope.$watch(
                function (scope) {
                    // watch the 'compile' expression for changes
                    return scope.$eval(attrs.compile);
                },
                function (value) {
                    // when the 'compile' expression changes
                    // assign it into the current DOM
                    element.html(value);

                    // compile the new DOM and link it to the current
                    // scope.
                    // NOTE: we only compile .childNodes so that
                    // we don't get into infinite loop compiling ourselves
                    $compile(element.contents())(scope);
                }
            );
        };
    }]);

    function replaceToken(encodedText) {
        var start = 0;
        do {
            start = encodedText.indexOf("${", start);
            var end = encodedText.indexOf("}$", end);
            if (start != -1 && end > start) {
                var token = encodedText.substring(start + 2, end - 1);
                console.log("token = '" + token + "'");
                var pcs = token.split("|");
                var symbol = pcs[0];
                var exchange = pcs[1];
                var changePct = pcs[2];
                console.log("symbol = " + symbol + ", exchange = " + exchange + ", changePct = " + changePct);
                encodedText = replace(encodedText, start, end + 2,
                    '<a href="" ng-click="loadNewsQuote(\'${symbol}\')">' +
                    '<nobr><span class="${exchange}">${symbol} ${changeArrow(change)}</span>' +
                    '<span class="${changeClass(changePct)}">${changePct}</span></nobr>' +
                    '</a>');
            }
            start = end;

        } while (start != -1 && start < encodedText.length);

        return encodedText;
    }

    function replace(source, start, end, replacement) {
        var s1 = source.substring(0, start);
        var s2 = source.substring(end, source.length);
        return s1 + replacement + s2;
    }

})();

