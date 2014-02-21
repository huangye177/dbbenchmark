
$(document).ready(function(){
            // ---------------- start of JQuery ready ----------------
        	
        	
            // start the timer
//            lineTimer.set(
//            {
//                time : timerMillSecond,
//                autostart : true
//            });

            // prepare SVG
        	
        	// load data
			jQuery.ajax(
            {
            	dataType: "json",
                url : "../dbbenchmark/defaultscenario",
                type : "GET",
            }).done(function(jsonObj)
            {
                var str = JSON.stringify(jsonObj, null, " ");
                console.log(str);
                
                jQuery("#jsonInput").val(str);
            });
			
			// on-click event
            d3.select("#startsimulation").on("click", function()
            {
                startSimulation();
            });

            // ---------------- end of JQuery ready ----------------
});

var startSimulation =  function() {
    
	var form = jQuery("#simulationform"); 
    
    form.submit();
    
}