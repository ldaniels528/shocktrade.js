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
         * @param w the given graph width
         * @param h the given graph height
         * @param r the given graph radius
         * @param data the given data array
         * @param elemId the ID of given element to populate (e.g. "#my_Graph")
         */
        service.pieChart = function (w, h, r, data, elemId) {
            // use the builtin range of colors
            var color = d3.scale.category20c();

            var vis = d3.select(elemId)
                .append("svg:svg")              //create the SVG element inside the <body>
                .data([data])                   //associate our data with the document
                .attr("width", w)           //set the width and height of our visualization (these will be attributes of the <svg> tag
                .attr("height", h)
                .append("svg:g")                //make a group to hold our pie chart
                .attr("transform", "translate(" + r + "," + r + ")");    //move the center of the pie chart from 0, 0 to radius, radius

            // this will create <path> elements for us using arc data
            var arc = d3.svg.arc().outerRadius(r);

            // this will create arc data for us given a list of values
            var pie = d3.layout.pie().value(function (d) {
                return d.value;
            });

            // we must tell it out to access the value of each element in our data array
            var arcs = vis.selectAll("g.slice")     //this selects all <g> elements with class slice (there aren't any yet)
                .data(pie)                          //associate the generated pie data (an array of arcs, each having startAngle, endAngle and value properties)
                .enter()                            //this will create <g> elements for every "extra" data element that should be associated with a selection. The result is creating a <g> for every object in the data array
                .append("svg:g")                //create a group to hold each slice (we will have a <path> and a <text> element associated with each slice)
                .attr("class", "slice");    //allow us to style things in the slices (like text)

            // set the color for each slice to be chosen from the color function defined below
            // this creates the actual SVG path using the associated data (pie) with the arc drawing function
            arcs.append("svg:path")
                .attr("fill", function (d, i) {
                    return color(i);
                })
                .attr("d", arc);

            arcs.append("svg:text")                                     //add a label to each slice
                .attr("transform", function (d) {                    //set the label's origin to the center of the arc
                    //we have to make sure to set these before calling arc.centroid
                    d.innerRadius = 0;
                    d.outerRadius = r;
                    return "translate(" + arc.centroid(d) + ")";        //this gives us a pair of coordinates like [50, 50]
                })
                .attr("text-anchor", "middle")                          //center the text on it's origin
                .text(function (d, i) {
                    return data[i].label;
                });        //get the label from our original data array
        };

        return service;
    });

})();