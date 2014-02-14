package org.itc.scenario;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScenarioUnit
{
    private String timeZone = "CET";
    private boolean batchMode = false;

    private int numOfInsertThread = 1;
    private int numOfQueryThread = 1;

    private String dbType = "";
    private String databaseDriver = "";
    private String databaseConnection = "";
    private String databaseUsername = "";
    private String databasePassword = "";

    private boolean enableDBCreate = false;
    private boolean enableDBInsert = false;
    private boolean enableDBQuery = false;
    private boolean enableDBDeletion = false;

    private List<ScenarioStatement> scenarioStatement = new ArrayList<ScenarioStatement>();

    public ScenarioUnit()
    {

    }
}
