package org.itc.scenario;

import lombok.Data;

@Data
public class ScenarioStatementResult
{

    private String scenarioStatementResultName = "";
    private long startTime = 0;
    private long currentTime = 0;
    private long endTime = 0;
    private double durationInSeconds = 0.0;
}
