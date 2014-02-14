package org.itc.concurrency;

import org.apache.commons.logging.LogFactory;
import org.itc.data.IStorageManager;
import org.itc.model.DBType;
import org.itc.model.InteroperateType;

public class DBQueryThread extends StorageCRUDThread
{
    public DBQueryThread(IStorageManager dbManager, int numSelectPerThread, DBType dbType, InteroperateType interoperatorType, String statementContent)
    {
        logger = LogFactory.getLog(DBQueryThread.class);

        this.dbManager = dbManager;
        this.numOperationsPerThread = numSelectPerThread;
        this.interoperatorType = interoperatorType;
        this.statementContent = statementContent;
        this.dbType = dbType;
        
    }

    @Override
    public void run()
    {
        this.dbReadScenario(this.dbManager, this.numOperationsPerThread, this.dbType, this.interoperatorType, this.statementContent);
    }
}
