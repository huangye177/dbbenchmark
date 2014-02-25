package org.itc.scenario;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScenarioResult {

	private String scenarioResultName = "";
	
	private List<ScenarioUnitResult> scenarioUnitResults = new ArrayList<ScenarioUnitResult>();
}
