package com.itc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ScenarioTrunk
{
    private IObserver traceObserver = new TraceObserver();
    private IObserver staticObserver = new StaticObserver();

    List<StorageCRUDThread> dbInsertThread = null;
    List<StorageCRUDThread> dbQueryThread = null;

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    Date today = new Date();

    public ScenarioTrunk()
    {
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        this.dbInsertThread = new ArrayList<StorageCRUDThread>();
        this.dbQueryThread = new ArrayList<StorageCRUDThread>();
    }

    public static void main(String[] args)
    {
        ScenarioTrunk scenarios = new ScenarioTrunk();
        scenarios.getTraceObserver().clearsysLog();
        scenarios.getStaticObserver().clearsysLog();

        scenarios.runScenarios();
    }

    public void runScenarios()
    {
        boolean enableDBCreate = true;
        boolean enableDBInsert = true;
        boolean enableDBQuery = true;
        boolean enableDBDeletion = true;

        /*
         * NOTICE: "Table Sharding" and "Partition" are mutual strategy here;
         * thus "partition > 1" only works if isTableShardingList is false
         */
        // boolean isTableSharding = false;
        boolean[] isTableShardingList = { false };
        int partition = 30;

        /*
         * NOTICE: Batch mode should run with Partition mode to given better
         * performance with better compability
         */
        boolean isBatchMode = true;

        // boolean[] isTableShardingList = { false, true };

        int numOfInsertThread = 1;
        int numOfQueryThread = 1;
        int numberOfSelections = 1000; // 1k queries to get average
        String[] dbTypeList = new String[] { "mysql" };
        // String[] dbTypeList = new String[] { "mongodb", "mysql" };
        boolean[] isSelectionWithIndexList = new boolean[] { true };
        // boolean[] isSelectionWithIndexList = new boolean[] { false, true };
        long[] dataSetSizeList = new long[] {
                10000 // TEST, 10K records
        // 3000000, // BENCHMARK, 3m records
        // 30000000, // 30m records
        // 60000000 // 60m records, est. MAX ADELE before 09.13
                // 100000000 // 100m records
        };

        for (boolean isTableSharding : isTableShardingList)
        {
            for (String dbType : dbTypeList)
            {
                for (int i = 0; i < dataSetSizeList.length; i++)
                {
                    long currentAmountOfMeasData = dataSetSizeList[i];
                    long lastAmountOfMeasData = -1;
                    long nextAmountOfMeasData = -1;

                    if ((i - 1) >= 0)
                    {
                        lastAmountOfMeasData = dataSetSizeList[i - 1];
                    }
                    else
                    {
                        lastAmountOfMeasData = -1;
                    }

                    if ((i + 1) < dataSetSizeList.length)
                    {
                        nextAmountOfMeasData = dataSetSizeList[i + 1];
                    }
                    else
                    {
                        nextAmountOfMeasData = -1;
                    }

                    if (lastAmountOfMeasData != currentAmountOfMeasData)
                    {
                        if (enableDBCreate)
                        {
                            /*
                             * create DB
                             */
                            this.dbCreatScenario(dbType, currentAmountOfMeasData, isTableSharding, partition);
                        }

                        if (enableDBInsert)
                        {
                            // insert tests
                            this.dbInsertThreadLaunch(dbType, currentAmountOfMeasData, numOfInsertThread,
                                    isTableSharding, partition, isBatchMode);
                        }
                    }
                    else
                    {
                        /*
                         * the same db type with same size was already created
                         * last time, thus no need to create a new data set for
                         * the same data storage type
                         */
                    }

                    /*
                     * query DB with different query options
                     */
                    if (enableDBQuery)
                    {
                        /*
                         * sleep for a few minutes thus machine CPU and Memory
                         * can be recycled to free status
                         */
                        try
                        {
                            Thread.sleep(1000 * 60 * 3);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }

                        this.dbReadThreadLaunch(dbType, currentAmountOfMeasData, numOfQueryThread, numberOfSelections, isSelectionWithIndexList,
                                isTableSharding, partition);
                    }

                    // delete data
                    if (nextAmountOfMeasData != currentAmountOfMeasData)
                    {
                        if (enableDBDeletion)
                        {
                            this.dbDeleteScenario(dbType, currentAmountOfMeasData, isTableSharding, partition);

                            /*
                             * sleep for a few minutes thus machine CPU and
                             * Memory can be recycled to free status
                             */
                            try
                            {
                                Thread.sleep(1000 * 60 * 5);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    else
                    {
                        /*
                         * do NOT delete the DB because it can be re-used by the
                         * next query scenario
                         */
                    }

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
    public void dbCreatScenario(String dbType, long amountOfMeasData, boolean isTableSharding, int partition)
    {
        IStorageManager dbManager = null;

        if (dbType.equalsIgnoreCase("mongodb"))
        {
            dbManager = new MongoDBManager(amountOfMeasData, isTableSharding);
        }
        else if (dbType.equalsIgnoreCase("mysql"))
        {
            dbManager = new MySQLManager(amountOfMeasData, isTableSharding, partition);
        }
        else
        {
            dbManager = null;
        }

        /*
         * initialize connection, db, and table/collection
         */
        dbManager.initConnection();

        Calendar cal = Calendar.getInstance();
        System.out.println("Table creating time: " + dateFormat.format(cal.getTime()) + " Table Sharding: " + isTableSharding);

        /*
         * create collection/indexes or table
         */
        dbManager.createMeasurementTable();

        /*
         * set db and collection to NULL
         */
        dbManager.closeConnection();
    }

    /**
     * Delete/drop DB/table in single thread
     */
    public void dbDeleteScenario(String dbType, long amountOfMeasData, boolean isTableSharding, int partition)
    {
        IStorageManager dbManager = null;

        if (dbType.equalsIgnoreCase("mongodb"))
        {
            dbManager = new MongoDBManager(amountOfMeasData, isTableSharding);
        }
        else if (dbType.equalsIgnoreCase("mysql"))
        {
            dbManager = new MySQLManager(amountOfMeasData, isTableSharding, partition);
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
         * drop measurement table
         */
        dbManager.dropMeasurementTable();

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
}
