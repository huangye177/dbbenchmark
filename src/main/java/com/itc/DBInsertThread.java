package com.itc;

import org.apache.commons.logging.LogFactory;

public class DBInsertThread extends StorageCRUDThread
{
    public DBInsertThread(String dbType, long amountOfMeasDataPerThread, boolean isTableSharding,
            int partition, boolean isBatchMode)
    {
        logger = LogFactory.getLog(DBInsertThread.class);

        this.amountOfMeasDataPerThread = amountOfMeasDataPerThread;
        this.dbType = dbType;
        this.isTableSharding = isTableSharding;
        this.partition = partition;
        this.isBatchMode = isBatchMode;
    }

    @Override
    public void run()
    {
        this.dbInsertScenario(this.dbType, this.amountOfMeasDataPerThread, this.isBatchMode);
    }
}
