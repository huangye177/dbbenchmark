package org.itc.concurrency;

import org.apache.commons.logging.LogFactory;
import org.itc.data.IStorageManager;
import org.itc.data.StorageCRUDThread;

public class DBQueryThread extends StorageCRUDThread
{
    private int numberOfSelections = 0;
    private boolean isSelectionWithIndex = false;

    public DBQueryThread(IStorageManager dbManager, int numSelectPerThread)
    {
        logger = LogFactory.getLog(DBQueryThread.class);

        this.dbManager = dbManager;
        this.numOperationsPerThread = numSelectPerThread;
    }

    @Override
    public void run()
    {
        this.dbReadScenario(this.dbManager, this.numOperationsPerThread);
    }
}
