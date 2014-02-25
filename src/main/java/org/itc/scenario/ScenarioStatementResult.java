package org.itc.scenario;

import java.util.Date;

import lombok.Data;

@Data
public class ScenarioStatementResult {

	private String scenarioStatementResultName = "";
	private Date startTime = null;
	private Date endTime = null;
	private double durationInSeconds = 0.0;
}
