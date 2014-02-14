package org.itc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.itc.concurrency.DBInsertThread;
import org.itc.concurrency.DBQueryThread;
import org.itc.data.IStorageManager;
import org.itc.data.MongoDBManager;
import org.itc.data.MySQLManager;
import org.itc.data.StorageCRUDThread;
import org.itc.model.DBFactory;
import org.itc.model.DBType;
import org.itc.model.OperationType;
import org.itc.scenario.IObserver;
import org.itc.scenario.JSONSettingReaderWriter;
import org.itc.scenario.ScenarioStatement;
import org.itc.scenario.ScenarioUnit;
import org.itc.scenario.Scenarios;
import org.itc.scenario.StaticObserver;
import org.itc.scenario.TraceObserver;

public class Trunk
{
    private static final Logger LOGGER = Logger.getLogger("Trunk");

    private IObserver traceObserver = new TraceObserver();
    private IObserver staticObserver = new StaticObserver();

    /** imported simulation scenarios from external setting file */
    private Scenarios scenarios = null;

    List<StorageCRUDThread> dbInsertThread = null;
    List<StorageCRUDThread> dbQueryThread = null;

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    Date today = new Date();

    public Trunk()
    {
        LOGGER.info("Hello, Gradle Application!");
        this.scenarios = JSONSettingReaderWriter.launchScenarioSetting();

        System.exit(0);

        this.dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        this.dbInsertThread = new ArrayList<StorageCRUDThread>();
        this.dbQueryThread = new ArrayList<StorageCRUDThread>();
    }

    public static void main(String[] args)
    {
        Trunk scenarioTrunk = new Trunk();
        scenarioTrunk.getTraceObserver().clearsysLog();
        scenarioTrunk.getStaticObserver().clearsysLog();

        scenarioTrunk.runScenarios();
    }

    public void runScenarios()
    {
        for (ScenarioUnit su : scenarios.getScenarioUnits())
        {
            /*
             * NOTICE: Batch mode should run with Partition mode to given better
             * performance with better compability
             */
            boolean isBatchMode = su.isBatchMode();
            boolean enableDBCreate = su.isEnableDBCreate();
            boolean enableDBInsert = su.isEnableDBInsert();
            boolean enableDBQuery = su.isEnableDBQuery();
            boolean enableDBDeletion = su.isEnableDBDeletion();

            int numOfInsertThread = su.getNumOfInsertThread();
            int numOfQueryThread = su.getNumOfQueryThread();
            // queries
            int numberOfSelections = su.getNumberOfSelections();
            String dbType = su.getDatabaseDriver();
            long dataSetSize = su.getDataSetSizeList();

            // create DB
            if (enableDBCreate)
            {
                this.dbCreatScenario(su);
            }

            // insert tests
            if (enableDBInsert)
            {
                this.dbInsertThreadLaunch(dbType, dataSetSize, numOfInsertThread,
                        false, -1, isBatchMode);
            }

            // query DB with different query options
            if (enableDBQuery)
            {
                /*
                 * sleep for a few minutes thus machine CPU and Memory can be
                 * recycled to free status
                 */
                try
                {
                    Thread.sleep(1000 * 60 * 1);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                this.dbReadThreadLaunch(dbType, dataSetSize, numOfQueryThread, numberOfSelections, null,
                        false, -1);
            }

            // delete data
            if (enableDBDeletion)
            {
                this.dbDeleteScenario(su);

                /*
                 * sleep for a few minutes thus machine CPU and Memory can be
                 * recycled to free status
                 */
                try
                {
                    Thread.sleep(1000 * 60 * 1);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Insert into DB in multi- threads
     */
    private void dbInsertThreadLaunch(String dbType, long amountOfMeasData, int numOfThread,
            boolean isTableSharding, int partition, boolean isBatchMode)
    {
        IStorageManager dbManagerForSizeCheck = null;

        if (dbType.equalsIgnoreCase("mongodb"))
        {
            dbManagerForSizeCheck = new MongoDBManager(amountOfMeasData, isTableSharding);
        }
        else if (dbType.equalsIgnoreCase("mysql"))
        {
            dbManagerForSizeCheck = new MySQLManager(amountOfMeasData, isTableSharding, partition);
        }
        else
        {
            dbManagerForSizeCheck = null;
        }
        dbManagerForSizeCheck.initConnection();

        Calendar cal = Calendar.getInstance();
        long startTime = System.currentTimeMillis();

        String info = "\n------------ SCENARIO-INSERT **START: " + " @Datetime: " + dateFormat.format(cal.getTime());
        info += "\nParam: [dbType]: " + dbType + "; [dataSize]: " + amountOfMeasData;
        this.staticObserver.update(info);

        /*
         * prepare all threads for insert task
         */
        this.dbInsertThread.clear();
        long amountOfMeasDataPerThread = amountOfMeasData / numOfThread;
        for (int i = 0; i < numOfThread; i++)
        {
            this.dbInsertThread.add(new DBInsertThread(dbType, amountOfMeasDataPerThread,
                    isTableSharding, partition, isBatchMode));
        }

        /*
         * start all threads for insert task
         */
        for (StorageCRUDThread thread : this.dbInsertThread)
        {
            thread.start();
        }

        System.out.println("INTOAL " + numOfThread + " THREADS STARTED!");

        /*
         * check results of all threads for insert task
         */
        while (!this.isThreadsExecuted(this.dbInsertThread))
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        double sizeKB = dbManagerForSizeCheck.getMeasurementDataSize();
        double indexKB = dbManagerForSizeCheck.getMeasurementDataIndexSize();
        dbManagerForSizeCheck.closeConnection();

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        double durationSecond = durationMillis / 1000;

        info = "\n[---DONE---] SCENARIO-INSERT *END: (" + dbType + "/" + amountOfMeasData + ") Execution duration: " + durationSecond + " seconds ("
                + durationMillis + " milliseocnds)";
        info += "\n" + dbType + " DB SIZE: " + (sizeKB / 1024) + "MB (" + (sizeKB / 1024 / 1024) + " GB /" + sizeKB + " KB)";
        info += "\n" + dbType + " DB INDEX SIZE: " + (indexKB / 1024) + "MB (" + (indexKB / 1024 / 1024) + " GB /" + indexKB + " KB)";

        this.dbInsertThread.clear();
        this.staticObserver.update(info, 0);
    }

    /**
     * Query from DB in multi- threads
     */
    private void dbReadThreadLaunch(String dbType, long amountOfMeasData, int numOfThread, int numberOfSelections, boolean[] isSelectionWithIndexList,
            boolean isTableSharding, int partition)
    {
        /*
         * make main thread to sleep a while to ensure MongoDB memory-dish flush
         * is done
         */
        try
        {
            Thread.sleep(3000);
        }
        catch (InterruptedException e1)
        {
            e1.printStackTrace();
        }

        for (boolean isSelectionWithIndex : isSelectionWithIndexList)
        {
            IStorageManager dbManagerForSizeCheck = null;

            if (dbType.equalsIgnoreCase("mongodb"))
            {
                dbManagerForSizeCheck = new MongoDBManager(amountOfMeasData, isTableSharding);
            }
            else if (dbType.equalsIgnoreCase("mysql"))
            {
                dbManagerForSizeCheck = new MySQLManager(amountOfMeasData, isTableSharding, partition);
            }
            else
            {
                dbManagerForSizeCheck = null;
            }
            dbManagerForSizeCheck.initConnection();

            Calendar cal = Calendar.getInstance();
            long startTime = System.currentTimeMillis();

            String info = "\n------------ SCENARIO-READ ++START: " + " @Datetime: " + dateFormat.format(cal.getTime());
            info += "\nParam: [dbType]: " + dbType + "; [dataSize]: " + amountOfMeasData + "; [numberOfSelections]: " + numberOfSelections
                    + "; [select_with_index?]:" + isSelectionWithIndex;
            this.staticObserver.update(info);

            /*
             * prepare all threads for query task
             */
            this.dbQueryThread.clear();
            long amountOfMeasDataPerThread = amountOfMeasData / numOfThread;
            for (int i = 0; i < numOfThread; i++)
            {
                this.dbQueryThread.add(new DBQueryThread(dbType, amountOfMeasDataPerThread, numberOfSelections,
                        isSelectionWithIndex, isTableSharding, partition));
            }

            /*
             * start all threads for query task
             */
            for (StorageCRUDThread thread : this.dbQueryThread)
            {
                thread.start();
            }

            System.out.println("INTOAL " + numOfThread + " THREADS STARTED!");

            /*
             * check results of all threads for query task
             */
            while (!this.isThreadsExecuted(this.dbQueryThread))
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            long totalQueryTime = 0;
            long totalQueryFetchTime = 0;
            for (StorageCRUDThread thread : this.dbQueryThread)
            {
                totalQueryTime += thread.getTotalQueryTime();
                totalQueryFetchTime += thread.getTotalQueryFetchTime();
            }

            double sizeKB = dbManagerForSizeCheck.getMeasurementDataSize();
            double indexKB = dbManagerForSizeCheck.getMeasurementDataIndexSize();
            dbManagerForSizeCheck.closeConnection();

            long endTime = System.currentTimeMillis();
            long durationMillis = endTime - startTime;
            double durationSecond = durationMillis / 1000;

            info = "\n[---DONE---] SCENARIO-READ +END: (" + dbType + "/" + amountOfMeasData + "-" + numberOfSelections + ") Execution duration: "
                    + durationSecond
                    + " seconds ("
                    + durationMillis + " milliseocnds)" + "; Total Query duration: "
                    + (totalQueryTime / 1000) + " seconds (" + totalQueryTime + " milliseocnds) "
                    + "; Total Query+Fetch duration: "
                    + (totalQueryFetchTime / 1000) + " seconds (" + totalQueryFetchTime + " milliseocnds) "
                    + ".";
            info += "\n" + dbType + " DB SIZE: " + (sizeKB / 1024) + "MB (" + (sizeKB / 1024 / 1024) + " GB /" + sizeKB + " KB)";
            info += "\n" + dbType + " DB INDEX SIZE: " + (indexKB / 1024) + "MB (" + (indexKB / 1024 / 1024) + " GB /" + indexKB + " KB)";

            this.dbQueryThread.clear();
            this.staticObserver.update(info, 0);
        }
    }

    /**
     * Create DB/table in single thread
     */
    public void dbCreatScenario(ScenarioUnit su)
    {
    	DBType dbType = DBType.valueOf(su.getDbType());
    	
        IStorageManager dbManager = DBFactory.buildDBManager(dbType);

        /*
         * initialize connection, db, and table/collection
         */
        dbManager.initConnection();

        Calendar cal = Calendar.getInstance();
        System.out.println("DB create operation time: " + dateFormat.format(cal.getTime()));

        for(ScenarioStatement stat : su.getScenarioStatement()) {
        	
        	OperationType operType = OperationType.valueOf(stat.getOperationtype());
        	
        	if(OperationType.CREATE.equals(operType)) {
        		/*
                 * create collection/indexes or table
                 */
        		dbManager.execCreateOperation(stat.getContent());
        	}
        }

        /*
         * set db and collection to NULL
         */
        dbManager.closeConnection();
    }

    /**
     * Delete/drop DB/table in single thread
     */
    public void dbDeleteScenario(ScenarioUnit su)
    {
    	DBType dbType = DBType.valueOf(su.getDbType());
    	
        IStorageManager dbManager = DBFactory.buildDBManager(dbType);

        /*
         * initialize connection, db, and table/collection
         */
        dbManager.initConnection();

        Calendar cal = Calendar.getInstance();
        System.out.println("DB delete operation time: " + dateFormat.format(cal.getTime()));

        for(ScenarioStatement stat : su.getScenarioStatement()) {
        	
        	OperationType operType = OperationType.valueOf(stat.getOperationtype());
        	
        	if(OperationType.DELETE.equals(operType)) {
        		/*
                 * create collection/indexes or table
                 */
        		dbManager.execDeleteOperation(stat.getContent());
        	}
        }

        /*
         * set db and collection to NULL
         */
        dbManager.closeConnection();

    }

    private boolean isThreadsExecuted(List<StorageCRUDThread> threads)
    {
        boolean isAllExecuted = true;

        for (StorageCRUDThread thread : threads)
        {
            isAllExecuted = (isAllExecuted && thread.isExecutionReady());
        }

        return isAllExecuted;
    }

    public IObserver getTraceObserver()
    {
        return traceObserver;
    }

    public IObserver getStaticObserver()
    {
        return staticObserver;
    }

    public Scenarios getScenarios()
    {
        return scenarios;
    }

    public void setScenarios(Scenarios scenarios)
    {
        this.scenarios = scenarios;
    }
}
