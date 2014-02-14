package org.itc.concurrency;

import org.apache.commons.logging.LogFactory;
import org.itc.data.IStorageManager;
import org.itc.data.StorageCRUDThread;

public class DBInsertThread extends StorageCRUDThread
{
    public DBInsertThread(IStorageManager dbManager, int numInsertPerThread)
    {
        logger = LogFactory.getLog(DBInsertThread.class);

        this.dbManager = dbManager;
        this.numOperationsPerThread = numInsertPerThread;
    }

    @Override
    public void run()
    {
        this.dbInsertScenario(this.dbManager, this.numOperationsPerThread);
    }
}
