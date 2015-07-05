(function() {
    var app = angular.module('shocktrade');

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
            '<nobr><span class="{{exchange}}">{{symbol}} {{change-arrow(change)}</span>' +
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
                    '<nobr><span class="${exchange}">${symbol} ${change-arrow(change)}</span>' +
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