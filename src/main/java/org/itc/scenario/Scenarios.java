package org.itc.scenario;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Scenarios
{
    private List<ScenarioUnit> scenarioUnits = new ArrayList<ScenarioUnit>();

    public Scenarios()
    {

    }
}
