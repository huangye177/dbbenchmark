package org.itc.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.itc.scenario.IObserver;
import org.itc.scenario.TraceObserver;

public class StorageCRUDThread extends Thread
{
    protected long amountOfMeasDataPerThread = 0;
    protected String dbType = "";

    protected boolean isTableSharding = false;
    protected int partition = 1;
    protected boolean isBatchMode = false;

    protected Log logger = null;
    volatile protected boolean executionReady = false;

    private long totalQueryTime = 0;
    private long totalQueryFetchTime = 0;

    private IObserver traceObserver = new TraceObserver();

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    public StorageCRUDThread()
    {
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
    }

    @Override
    public void run()
    {

    }

    protected void dbInsertScenario(String dbType, long amountOfMeasData, boolean isBatchMode)
    {
        this.executionReady = false;

        IStorageManager dbManager = null;

        if (dbType.equalsIgnoreCase("mongodb"))
        {
            dbManager = new MongoDBManager(amountOfMeasData, this.isTableSharding);
        }
        else if (dbType.equalsIgnoreCase("mysql"))
        {
            dbManager = new MySQLManager(amountOfMeasData, this.isTableSharding, this.partition);
        }
        else
        {
            dbManager = null;
        }

        /*
         * initialize connection, db, and table/collection
         */
        dbManager.initConnection();

        /*
         * insert all measurement data
         */
        if (isBatchMode)
        {
            dbManager.insertMeasurementsInBatch();
        }
        else
        {
            dbManager.insertMeasurements();
        }

        dbManager.closeConnection();

        /*
         * set the execution mark as done
         */
        this.executionReady = true;

    }

    protected void dbReadScenario(String dbType, long amountOfMeasData, int numberOfSelections, boolean isSelectionWithIndex)
    {
        this.executionReady = false;
        this.totalQueryTime = 0;
        this.totalQueryFetchTime = 0;

        IStorageManager dbManager = null;

        if (dbType.equalsIgnoreCase("mongodb"))
        {
            dbManager = new MongoDBManager(amountOfMeasData, this.isTableSharding);
        }
        else if (dbType.equalsIgnoreCase("mysql"))
        {
            dbManager = new MySQLManager(amountOfMeasData, this.isTableSharding, this.partition);
        }
        else
        {
            dbManager = null;
        }
        dbManager.registerObserver(traceObserver);

        /*
         * initialize connection, db, and table/collection
         */
        dbManager.initConnection();

        /*
         * read data
         */
        if (isSelectionWithIndex)
        {
            for (int j = 0; j < numberOfSelections; j++)
            {
                long[] totalTime = dbManager.selectMeasurementByDataSeriesId();
                this.totalQueryTime += totalTime[0];
                this.totalQueryFetchTime += totalTime[1];
            }
        }
        else
        {
            for (int j = 0; j < numberOfSelections; j++)
            {
                long[] totalTime = dbManager.selectMeasurementByProjectId();
                this.totalQueryTime += totalTime[0];
                this.totalQueryFetchTime += totalTime[1];
            }
        }

        /*
         * set the execution mark as done
         */
        this.executionReady = true;

    }

    public synchronized boolean isExecutionReady()
    {
        return this.executionReady;
    }

    public long getTotalQueryTime()
    {
        return totalQueryTime;
    }

    public long getTotalQueryFetchTime()
    {
        return totalQueryFetchTime;
    }

}
