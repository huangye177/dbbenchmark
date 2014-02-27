var margin =
{
    top : 20,
    right : 20,
    bottom : 300,
    left : 50
}, width = 600 - margin.left - margin.right, height = 900 - margin.top - margin.bottom;

var barWidth = 100;
var parseJSONDate = d3.time.format("%Y-%m-%d").parse;

var timerMillSecond = 1000;
var tickCount = 6;
var allowDataUpdate = true;

var xScale = d3.scale.ordinal().rangeRoundBands([0, width], .1);

var yScale = d3.scale.linear().range([ height, 0 ]);

var color = d3.scale.category20();

var xAxis = d3.svg.axis();

var yAxis = d3.svg.axis();

var initialData = [];
var initialScenarios = [];

var svg;

/*
 * get updated data from RESTful and change HTML element accordingly 
 */
var refreshLineData = function()
{
    
    // load update data
    data_update_request = $.ajax(
    {
        url : "../dbbenchmark/getsimulation",
        async: true,
        type : "GET"
    });

    initialData = [];
    
    data_update_request.done(function(jsonObj)
    {
        if(jsonObj.finished) {
            jQuery("#expfinished").html("Done!"); 
        } else {
            jQuery("#expfinished").html("Ongoing..."); 
        }
        
        
        // request simulation result
        sim_counter_request = $.ajax(
        {
            url : "../dbbenchmark/countsimulation",
            type : "GET"
        });
        
        sim_counter_request.done(function(counter)
        {
            initialScenarios = counter;
        });
        
        // load data from returned JSON
        $.each(jsonObj.scenarioUnitResults, function(key, value) {
            
            var scenarioUnitResults = value;
            
            $.each(scenarioUnitResults.scenarioStatementResults, function(key1, value1) {
                
                var scenarioStatementResult = value1;
                
                initialData.push(
                        {
                            scenario : scenarioStatementResult.scenarioStatementResultName,
                            start : scenarioStatementResult.startTime,
                            current : scenarioStatementResult.currentTime, 
                            second : scenarioStatementResult.durationInSeconds
                        });
                
            });
            
        });
        
        // identify svg
        svg = d3.select("#bar_svg_id");
        
        
        // set axis domain
        xScale.domain(initialScenarios);
        
        if(jsonObj.finished) {
            yScale.domain([0, d3.max(initialData, function(d){
                return d.second;
            })]); 
        } else {
            yScale.domain([0, d3.max(initialData, function(d){
                return (d.current - d.start) / 1000;
            })]);
        }
        
        
//        xAxis.scale(xScale).orient("bottom");
        yAxis.scale(yScale).orient("left").tickFormat(d3.format(".2s"));

        svg.selectAll("#x-axis-id").call(xAxis);
        svg.selectAll("#y-axis-id").call(yAxis);
        
        var bars = svg.selectAll(".bar").data(initialData);
        
        console.log(JSON.stringify(initialData));
        
        // redraw
        svg.selectAll("rect").data(initialData)
        .style("fill", function(d) { 
            console.log("test" + initialData);
            return color(d.scenario); 
         })
        .attr("x", function(d) { 
            return xScale(d.scenario); 
        })
        .attr("width", xScale.rangeBand())
        .attr("y", function(d) { 
            console.log("B y:" + (d.current - d.start) ); 
            timeDiff = (d.current - d.start) / 1000;
            return yScale(timeDiff); 
         })
        .attr("height", function(d) { 
            return height - yScale((d.current - d.start) / 1000); 
         });
        
    });
};

/*
 * set timer
 */
var lineTimer = $.timer(refreshLineData);

$(document).ready(
        function()
        {
            // ---------------- start of JQuery ready ----------------

            // start the timer
            lineTimer.set(
            {
                time : timerMillSecond,
                autostart : true
            });
            
            initialData = [];
            initialScenarios = [];
            
            // start simulation 
            run_sim_request = $.ajax({
                url : "../dbbenchmark/runsimulation",
                async: true,
                type : "GET"
             });
            
            console.log("call switch");
            
            // request simulation setup
            sim_counter_request = $.ajax(
            {
                url : "../dbbenchmark/countsimulation",
                type : "GET"
            });
            sim_counter_request.done(function(counter)
            {
                if(width < (barWidth * counter.length)) {
                    width = barWidth * counter.length;
                    xScale = d3.scale.ordinal().rangeRoundBands([0, width], .1);
                }
                
                initialScenarios = counter;
                
                $.each(counter, function(key, value) {
                    initialData.push(
                            {
                                scenario : value,
                                start : 0, 
                                current : 0,
                                second : 0
                            });
                });
            });
            
            
            
            // request simulation result
            data_update_request = $.ajax(
            {
                url : "../dbbenchmark/getsimulation",
                type : "GET"
            });
            data_update_request.done(function(jsonObj)
            {
                // load data from returned JSON
                $.each(jsonObj.scenarioUnitResults, function(key, value) {
                    
                    var scenarioUnitResults = value;
                    
                    $.each(scenarioUnitResults.scenarioStatementResults, function(key1, value1) {
                        
                        var scenarioStatementResult = value1;
                        
                        initialScenarios.push(scenarioStatementResult.scenarioStatementResultName);
                        
                        initialData.push(
                        {
                            scenario : scenarioStatementResult.scenarioStatementResultName,
                            start : scenarioStatementResult.startTime,
                            current : scenarioStatementResult.currentTime, 
                            second : scenarioStatementResult.durationInSeconds
                        });
                        
                    });
                    
                });
                
                // prepare SVG
                svg = d3.select("body").append("svg").attr("id", "bar_svg_id")
                .attr("xmlns", "http://www.w3.org/2000/svg")
                        .attr("width", width + margin.left + margin.right).attr("height", height + margin.top + margin.bottom).append(
                        "g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                
                // set axis domain
                xScale.domain(initialScenarios);
                
                yScale.domain([0, d3.max(initialData, function(d){
//                    return d.second;
                    return d.current - d.start;
                })]);
                
                /*
                 * plot axis
                 */
                xAxis.scale(xScale).orient("bottom");
                yAxis.scale(yScale).orient("left").tickFormat(d3.format(".2s"));

                // draw axis
                svg.append("g").attr("id", "x-axis-id").attr("class", "x axis").attr("transform", "translate(0," + height + ")")
                .call(xAxis)
                .selectAll("text")  
                .style("text-anchor", "end")
                .attr("dx", "-.8em")
                .attr("dy", ".15em")
                .attr("transform", function(d) {
                    return "rotate(-65)" 
                    });

                svg.append("g").attr("id", "y-axis-id").attr("class", "y axis").call(yAxis).append("text").attr("transform", "rotate(-90)").attr("y", 6).attr("dy", ".71em").style(
                        "text-anchor", "end").text("Seconds");

                // draw bars
                svg.selectAll("rect").data(initialData).enter().append("rect")
                .attr("class", "bar")
                .style("fill", function(d) { return color(d.scenario); })
                .attr("x", function(d) { return xScale(d.scenario); })
                .attr("width", xScale.rangeBand())
                .attr("y", function(d) { return yScale(d.current - d.start); })
                .attr("height", function(d) { return height - yScale(d.current - d.start); });
//                .attr("y", function(d) { return yScale(d.second); })
//                .attr("height", function(d) { return height - yScale(d.second); });
                
                // on-click event
                d3.select("#endsimulation").on("click", function()
                {
                    endSimulation();
                });
                

            });

            // ---------------- end of JQuery ready ----------------

        });

var endSimulation =  function() {
    
    var form = jQuery("#endSimulation"); 
    
    form.submit();
    
}