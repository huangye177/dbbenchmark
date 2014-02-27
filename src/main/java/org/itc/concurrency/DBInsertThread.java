package org.itc.concurrency;

import org.apache.commons.logging.LogFactory;
import org.itc.data.IStorageManager;
import org.itc.model.DBType;
import org.itc.model.InteroperateType;
import org.itc.scenario.ScenarioStatementResult;

public class DBInsertThread extends StorageCRUDThread
{
    public DBInsertThread(IStorageManager dbManager, int numInsertPerThread, DBType dbType, InteroperateType interoperatorType, Object statementContent,
            ScenarioStatementResult scenarioStatementResult)
    {
        logger = LogFactory.getLog(DBInsertThread.class);

        this.dbManager = dbManager;
        this.numOperationsPerThread = numInsertPerThread;
        this.dbType = dbType;
        this.interoperatorType = interoperatorType;
        this.statementContent = statementContent;
        this.scenarioStatementResult = scenarioStatementResult;

    }

    @Override
    public void run()
    {
        this.dbInsertScenario(this.dbManager, this.numOperationsPerThread, this.dbType, this.interoperatorType, this.statementContent);
    }
}
