package org.itc.concurrency;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.itc.data.IStorageManager;
import org.itc.model.DBType;
import org.itc.model.DataInteroperatorFactory;
import org.itc.model.IDataInteroperator;
import org.itc.model.InteroperateType;
import org.itc.model.OperationType;
import org.itc.scenario.IObserver;
import org.itc.scenario.TraceObserver;

public class StorageCRUDThread extends Thread
{

    protected IStorageManager dbManager = null;
    protected int numOperationsPerThread = 0;

    protected DBType dbType = null;
    protected InteroperateType interoperatorType = null;
    protected Object statementContent = "";

    // --

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

    protected void dbInsertScenario(IStorageManager dbManager,
            int numInsertPerThread, DBType dbType, InteroperateType interoperatorType, Object statementContent)
    {

        this.executionReady = false;

        // NOTICE: DB connection should be managed out of thread
        for (int i = 0; i < numInsertPerThread; i++)
        {
            Object interoperatedObject = null;

            if (interoperatorType != null)
            {
                IDataInteroperator interoperator = DataInteroperatorFactory.buildDataInteroperator(interoperatorType);
                interoperatedObject = interoperator.interoperate(statementContent, dbType, OperationType.INSERT);
            }
            else
            {
                interoperatedObject = statementContent;
            }

            dbManager.execInsertOperation(interoperatedObject);
        }

        /*
         * set the execution mark as done
         */
        this.executionReady = true;

    }

    protected void dbReadScenario(IStorageManager dbManager,
            int numSelectPerThread, DBType dbType, InteroperateType interoperatorType, Object statementContent)
    {

        this.executionReady = false;

        dbManager.registerObserver(traceObserver);

        for (int i = 0; i < numSelectPerThread; i++)
        {
            Object interoperatedObject = null;

            if (interoperatorType != null)
            {
                IDataInteroperator interoperator = DataInteroperatorFactory.buildDataInteroperator(interoperatorType);
                interoperatedObject = interoperator.interoperate(statementContent, dbType, OperationType.SELECT);
            }
            else
            {
                interoperatedObject = statementContent;
            }

            dbManager.execSelectOperation(interoperatedObject);
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
