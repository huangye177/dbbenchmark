package org.itc.scenario;

import lombok.Data;

@Data
public class ScenarioStatement
{
    private String operationtype = "";
    private int repeat = 1;
    private String interoperate = "";
    private Object content = "";
    private int batchSize = 1;
    private int fetchSize = 0;

    public ScenarioStatement()
    {

    }
}
