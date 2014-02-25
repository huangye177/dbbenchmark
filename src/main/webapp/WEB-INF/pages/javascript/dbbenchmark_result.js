
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
                url : "../dbbenchmark/runsimulation",
                type : "GET",
            }).done(function(jsonObj)
            {
                var str = JSON.stringify(jsonObj, null, " ");
                console.log(str);
                
                jQuery("#jsonInput").val(str);
            });
			
			// on-click event
            d3.select("#endsimulation").on("click", function()
            {
                endSimulation();
            });

            // ---------------- end of JQuery ready ----------------
});

var endSimulation =  function() {
    
	var form = jQuery("#endSimulation"); 
    
    form.submit();
    
}