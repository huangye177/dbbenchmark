package org.itc.scenario;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ScenarioUnitResult {
	
	private String scenarioUnitResultName = "";
	private Date startTime = null;
	private Date endTime = null;
	private double durationInSeconds = 0.0;
	
	private List<ScenarioStatementResult> scenarioStatementResults = new ArrayList<ScenarioStatementResult>();
}
