package com.itc;

import org.apache.commons.logging.LogFactory;

public class DBQueryThread extends StorageCRUDThread
{
    private int numberOfSelections = 0;
    private boolean isSelectionWithIndex = false;

    public DBQueryThread(String dbType, long amountOfMeasDataPerThread,
            int numberOfSelections, boolean isSelectionWithIndex, boolean isTableSharding, int partition)
    {
        logger = LogFactory.getLog(DBQueryThread.class);

        this.amountOfMeasDataPerThread = amountOfMeasDataPerThread;
        this.dbType = dbType;
        this.numberOfSelections = numberOfSelections;
        this.isSelectionWithIndex = isSelectionWithIndex;
        this.isTableSharding = isTableSharding;
        this.partition = partition;
    }

    @Override
    public void run()
    {
        this.dbReadScenario(dbType, amountOfMeasDataPerThread, numberOfSelections, isSelectionWithIndex);
    }
}
