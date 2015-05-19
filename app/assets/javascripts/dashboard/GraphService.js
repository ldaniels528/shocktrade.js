(function () {
    var app = angular.module('shocktrade');

    /**
     * Graph Service
     * @author lawrence.daniels@gmail.com
     */
    app.factory('GraphService', function ($log) {
        var service = {};

        /**
         * Generates a pie chart graph
         * @param width the given graph width
         * @param height the given graph height
         * @param chartData the given chart data array
         * @param elemId the ID of given element to populate (e.g. "#my_Graph")
         */
        service.pieChart = function(width, height, chartData, elemId) {
            var svg = d3.select(elemId).append("svg").append("g");
            svg.append("g").attr("class", "slices");
            svg.append("g").attr("class", "labels");
            svg.append("g").attr("class", "lines");

            /*
            var w = $(elemId).outerWidth(false);
            var h = $(elemId).outerHeight();
            $log.info(elemId + ": width = " + w + ", height = " + h);
            */

            // compute the radius
            var radius = Math.min(width, height) * 0.40;

            var pie = d3.layout.pie()
                .sort(null)
                .value(function(d) {
                    return d.value;
                });

            var arc = d3.svg.arc()
                .outerRadius(radius * 0.8)
                .innerRadius(radius * 0.4);

            var outerArc = d3.svg.arc()
                .innerRadius(radius * 0.9)
                .outerRadius(radius * 0.9);

            svg.attr("transform", "translate(" + width * 0.50 + "," + height * 0.40 + ")");

            var key = function(d){ return d.data.label; };

            var color = d3.scale.ordinal()
                .domain(["Lorem ipsum", "dolor sit", "amet", "consectetur", "adipisicing", "elit", "sed", "do", "eiusmod", "tempor", "incididunt"])
                .range(["#ff8888", "#88ffff", "#8888ff", "#ff8800", "#00ff00", "#88ffaa", "#ff88ff"]);

            generatePieChart(svg, chartData, key, pie, radius, arc, outerArc, color, this);
        };

        function generatePieChart(svg, data, key, pie, radius, arc, outerArc, color, self) {
            makeSlices(svg, data, key, pie, arc, color, self);
            makeText(svg, data, key, pie, arc, radius, outerArc, self);
            makePolyLines(svg, data, key, pie, arc, radius, outerArc, self);
        }

        function makeSlices(svg, data, key, pie, arc, color, self) {
            var slice = svg.select(".slices").selectAll("path.slice").data(pie(data), key);
            slice.enter()
                .insert("path")
                .style("fill", function(d) { return color(d.data.label); })
                .attr("class", "slice");

            slice
                .transition().duration(1000)
                .attrTween("d", function(d) {
                    self._current = self._current || d;
                    var interpolate = d3.interpolate(self._current, d);
                    self._current = interpolate(0);
                    return function(t) {
                        return arc(interpolate(t));
                    };
                });

            slice.exit().remove();
        }

        function makeText(svg, data, key, pie, arc, radius, outerArc, self) {
            var text = svg.select(".labels").selectAll("text").data(pie(data), key);
            text.enter()
                .append("text")
                .attr("dy", ".35em")
                .text(function(d) {
                    return d.data.label;
                });

            text.transition().duration(1000)
                .attrTween("transform", function(d) {
                    self._current = self._current || d;
                    var interpolate = d3.interpolate(self._current, d);
                    self._current = interpolate(0);
                    return function(t) {
                        var d2 = interpolate(t);
                        var pos = outerArc.centroid(d2);
                        pos[0] = radius * (midAngle(d2) < Math.PI ? 1 : -1);
                        return "translate("+ pos +")";
                    };
                })
                .styleTween("text-anchor", function(d){
                    self._current = self._current || d;
                    var interpolate = d3.interpolate(self._current, d);
                    self._current = interpolate(0);
                    return function(t) {
                        var d2 = interpolate(t);
                        return midAngle(d2) < Math.PI ? "start":"end";
                    };
                });

            text.exit().remove();
        }

        function makePolyLines(svg, data, key, pie, arc, radius, outerArc, self) {
            var polyLine = svg.select(".lines").selectAll("polyline").data(pie(data), key);
            polyLine.enter().append("polyline");
            polyLine.transition().duration(1000)
                .attrTween("points", function(d){
                    self._current = self._current || d;
                    var interpolate = d3.interpolate(self._current, d);
                    self._current = interpolate(0);
                    return function(t) {
                        var d2 = interpolate(t);
                        var pos = outerArc.centroid(d2);
                        pos[0] = radius * 0.95 * (midAngle(d2) < Math.PI ? 1 : -1);
                        return [arc.centroid(d2), outerArc.centroid(d2), pos];
                    };
                });

            polyLine.exit().remove();
        }

        function midAngle(d){
            return d.startAngle + (d.endAngle - d.startAngle)/2;
        }

        return service;
    });

})();