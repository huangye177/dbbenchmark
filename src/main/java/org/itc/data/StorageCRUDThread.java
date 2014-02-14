package org.itc.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.itc.scenario.IObserver;
import org.itc.scenario.TraceObserver;

public class StorageCRUDThread extends Thread
{
	protected IStorageManager dbManager = null;
	protected int numOperationsPerThread = 0;

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

    protected void dbInsertScenario(IStorageManager dbManager, int numInsertPerThread)
    {
        this.executionReady = false;
        
        // NOTICE: db connection should be managed out of thread
            dbManager.insertMeasurements();

        /*
         * set the execution mark as done
         */
        this.executionReady = true;

    }

    protected void dbReadScenario(IStorageManager dbManager, int numInsertPerThread)
    {
        this.executionReady = false;
        this.totalQueryTime = 0;
        this.totalQueryFetchTime = 0;

        dbManager.registerObserver(traceObserver);

        dbManager.selectMeasurementByDataSeriesId();

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
