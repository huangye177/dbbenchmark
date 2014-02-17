package org.itc.scenario;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScenarioUnit
{
    private String timeZone = "CET";

    private int numOfInsertThread = 1;
    private int numOfQueryThread = 1;

    private String databaseType = "";
    private String databaseDriver = "";
    private String databaseConnection = "";
    private String databaseUsername = "";
    private String databasePassword = "";

    private List<ScenarioStatement> scenarioStatement = new ArrayList<ScenarioStatement>();

    public ScenarioUnit()
    {

    }
}
