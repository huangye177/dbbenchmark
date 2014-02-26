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
import org.itc.concurrency.StorageCRUDThread;
import org.itc.data.IStorageManager;
import org.itc.model.DBFactory;
import org.itc.model.DBType;
import org.itc.model.InteroperateType;
import org.itc.model.OperationType;
import org.itc.scenario.IObserver;
import org.itc.scenario.JSONSettingReaderWriter;
import org.itc.scenario.ScenarioResult;
import org.itc.scenario.ScenarioStatement;
import org.itc.scenario.ScenarioStatementResult;
import org.itc.scenario.ScenarioUnit;
import org.itc.scenario.ScenarioUnitResult;
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

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private Date today = new Date();
    private ScenarioResult scenarioResult = new ScenarioResult();
    private List<String> allScenarios = new ArrayList<String>();

    /**
     * Start simulation in local-mode
     */
    public Trunk()
    {
        LOGGER.info("Hello, Gradle Application!");
        this.scenarios = JSONSettingReaderWriter.launchScenarioSetting(null);

        this.dbInsertThread = new ArrayList<StorageCRUDThread>();
        this.dbQueryThread = new ArrayList<StorageCRUDThread>();

        this.analyzeAllScenarios();
    }

    /**
     * Start simulation in web-mode
     */
    public Trunk(String inputScenarioJSONString)
    {
        LOGGER.info("Hello, Gradle Application!");
        this.scenarios = JSONSettingReaderWriter.launchScenarioSetting(inputScenarioJSONString);

        this.dbInsertThread = new ArrayList<StorageCRUDThread>();
        this.dbQueryThread = new ArrayList<StorageCRUDThread>();

        this.analyzeAllScenarios();
    }

    public static void main(String[] args)
    {
        Trunk scenarioTrunk = new Trunk();
        scenarioTrunk.getTraceObserver().clearsysLog();
        scenarioTrunk.getStaticObserver().clearsysLog();

        scenarioTrunk.runScenarios();

        System.out.println(scenarioTrunk.getScenarioResult());

    }

    /**
     * Generate all scneario names as the place holder for javascript xAxis
     */
    public void analyzeAllScenarios()
    {
        this.allScenarios.clear();
        int scenarioStatementCounter = 0;
        for (ScenarioUnit su : scenarios.getScenarioUnits())
        {
            if ("COMMENT".equals(su.getScenarioName()))
            {
                continue;
            }

            for (ScenarioStatement stat : su.getScenarioStatement())
            {
                scenarioStatementCounter++;
                String placeHolder = this.generateScenarioResultName(scenarioStatementCounter, su, stat);

                this.allScenarios.add(placeHolder);
            }
        }
    }

    /**
     * Generate a scenario result name
     * 
     * @param seriesNr
     * @param su
     * @param stat
     * @return
     */
    private String generateScenarioResultName(int seriesNr, ScenarioUnit su, ScenarioStatement stat)
    {
        return "[" + seriesNr + "]" + su.getScenarioName() + "." + stat.getOperationtype()
                + "(" + ((double) stat.getRepeat()) / 1000 + "K)";
    }

    public void runScenarios()
    {
        if (this.scenarioResult.isStarted() && (!this.scenarioResult.isFinished()))
        {
            return;
        }

        this.scenarioResult = new ScenarioResult();
        scenarioResult.setStarted(true);

        int scenarioStatementCounter = 0;
        for (ScenarioUnit su : scenarios.getScenarioUnits())
        {
            String scenarioName = su.getScenarioName();

            if ("COMMENT".equals(scenarioName))
            {
                continue;
            }

            // initialize scenarioUnitResult
            ScenarioUnitResult scenarioUnitResult = new ScenarioUnitResult();
            scenarioUnitResult.setScenarioUnitResultName(su.getScenarioName());
            scenarioUnitResult.setStartTime(new Date());

            long scenarioUnitStartTime = System.currentTimeMillis();
            this.dateFormat.setTimeZone(TimeZone.getTimeZone(su.getTimeZone()));
            Calendar cal = Calendar.getInstance();

            DBType dbType = DBType.valueOf(su.getDatabaseType());

            IStorageManager dbManager = DBFactory.buildDBManager(dbType, su.getDatabaseConnection(),
                    su.getDatabaseUsername(), su.getDatabasePassword());

            /*
             * initialize database connection
             */
            dbManager.initConnection();

            for (ScenarioStatement stat : su.getScenarioStatement())
            {
                scenarioStatementCounter++;
                cal.setTime(new Date());
                double numOfRepeastInThousand = ((double) stat.getRepeat()) / 1000;

                // initialize ScenarioStatementResult
                ScenarioStatementResult scenarioStatementResult = new ScenarioStatementResult();
                scenarioStatementResult.setScenarioStatementResultName(this.generateScenarioResultName(scenarioStatementCounter, su, stat));
                scenarioStatementResult.setStartTime(new Date());

                String info = "\n[---START Scenario " + scenarioName + " (" + numOfRepeastInThousand + "K) ---] " + su.getDatabaseType() + " DB "
                        + stat.getOperationtype() + " scenario started at: " + dateFormat.format(cal.getTime());
                long startTime = System.currentTimeMillis();

                this.staticObserver.update(info);

                OperationType operType = OperationType.valueOf(stat.getOperationtype());

                // execute DB scenario
                if (OperationType.CREATE.equals(operType))
                {
                    dbManager.execCreateOperation(stat.getContent());
                }
                else if (OperationType.DELETE.equals(operType))
                {
                    dbManager.execDeleteOperation(stat.getContent());
                }
                else if (OperationType.INSERT.equals(operType))
                {
                    dbManager.setBatchSize(stat.getBatchSize());

                    this.dbInsertThread.clear();

                    int numInsert = stat.getRepeat();
                    int numInsertPerThread = numInsert / su.getNumOfInsertThread();

                    // prepare all threads for insert task
                    for (int i = 0; i < su.getNumOfInsertThread(); i++)
                    {
                        InteroperateType interoperaterType = (stat.getInteroperate() == null) ? null : InteroperateType.valueOf(stat.getInteroperate());

                        this.dbInsertThread.add(new DBInsertThread(dbManager,
                                numInsertPerThread,
                                DBType.valueOf(su.getDatabaseType()),
                                interoperaterType,
                                stat.getContent()));
                    }

                    // start all threads for insert task
                    for (StorageCRUDThread thread : this.dbInsertThread)
                    {
                        thread.start();
                    }

                    // check results of all threads for insert task
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

                    this.dbInsertThread.clear();

                }
                else if (OperationType.SELECT.equals(operType))
                {
                    dbManager.setFetchSize(stat.getFetchSize());

                    this.dbQueryThread.clear();

                    int numSelect = stat.getRepeat();
                    int numSelectPerThread = numSelect / su.getNumOfQueryThread();

                    // prepare all threads for select task
                    for (int i = 0; i < su.getNumOfInsertThread(); i++)
                    {
                        InteroperateType interoperaterType = (stat.getInteroperate() == null) ? null : InteroperateType.valueOf(stat.getInteroperate());

                        this.dbQueryThread.add(new DBQueryThread(dbManager,
                                numSelectPerThread,
                                DBType.valueOf(su.getDatabaseType()),
                                interoperaterType,
                                stat.getContent()));
                    }

                    // start all threads for select task
                    for (StorageCRUDThread thread : this.dbQueryThread)
                    {
                        thread.start();
                    }

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

                    this.dbQueryThread.clear();
                }
                else if (OperationType.COMMENT.equals(operType))
                {
                    // do nothing for OperationType: COMMENT
                }

                // logging
                long endTime = System.currentTimeMillis();
                long durationMillis = endTime - startTime;
                double durationSecond = durationMillis / 1000;

                // double sizeKB = dbManager.getMeasurementDataSize();
                // double indexKB = dbManager.getMeasurementDataIndexSize();

                cal.setTime(new Date());
                info = "\n[---DONE " + scenarioName + "---] SCENARIO-" + stat.getOperationtype() + " END at " + dateFormat.format(cal.getTime()) + "; ("
                        + dbType + ") Execution duration: " + durationSecond + " seconds (" + durationMillis + " milliseocnds)";

                // info += "\n" + dbType + " DB SIZE: " + (sizeKB / 1024) +
                // "MB (" + (sizeKB / 1024 / 1024) + " GB /" + sizeKB + " KB)";
                // info += "\n" + dbType + " DB INDEX SIZE: " + (indexKB / 1024)
                // + "MB (" + (indexKB / 1024 / 1024) + " GB /" + indexKB +
                // " KB)";

                this.staticObserver.update(info, 0);

                // set scenarioStatementResult
                scenarioStatementResult.setEndTime(new Date());
                scenarioStatementResult.setDurationInSeconds(durationSecond);

                scenarioUnitResult.getScenarioStatementResults().add(scenarioStatementResult);
            }

            /*
             * close database connection
             */
            dbManager.closeConnection();

            dbManager = null;

            long scenarioUnitEndTime = System.currentTimeMillis();
            double scenarioUnitDurationSecond = (scenarioUnitEndTime - scenarioUnitStartTime) / 1000;

            // set scenarioUnitResult
            scenarioUnitResult.setEndTime(new Date());
            scenarioUnitResult.setDurationInSeconds(scenarioUnitDurationSecond);
            this.scenarioResult.getScenarioUnitResults().add(scenarioUnitResult);

        }

        this.scenarioResult.setFinished(true);

    }

    /**
     * Insert into DB in multi- threads
     */
    private void dbInsertThreadLaunch(ScenarioUnit su)
    {
        DBType dbType = DBType.valueOf(su.getDatabaseType());

        IStorageManager dbManager = DBFactory.buildDBManager(dbType, su.getDatabaseConnection(),
                su.getDatabaseUsername(), su.getDatabasePassword());

        Calendar cal = Calendar.getInstance();
        long startTime = System.currentTimeMillis();

        String info = "\n------------ SCENARIO-INSERT **START: "
                + " @Datetime: " + dateFormat.format(cal.getTime());
        info += "\nParam: [dbType]: " + dbType;
        this.staticObserver.update(info);

        // launch each insert operation with single-/multi- thread
        this.dbInsertThread.clear();
        for (ScenarioStatement stat : su.getScenarioStatement())
        {

            OperationType operType = OperationType.valueOf(stat
                    .getOperationtype());

            if (OperationType.INSERT.equals(operType))
            {

                int numInsert = stat.getRepeat();
                int numInsertPerThread = numInsert / su.getNumOfInsertThread();

                this.dbInsertThread.add(new DBInsertThread(dbManager,
                        numInsertPerThread,
                        DBType.valueOf(su.getDatabaseType()),
                        InteroperateType.valueOf(stat.getInteroperate()),
                        stat.getContent()));
            }
        }

        // initial a DB connection
        dbManager.initConnection();

        /*
         * start all threads for insert task
         */
        for (StorageCRUDThread thread : this.dbInsertThread)
        {
            thread.start();
        }

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

        double sizeKB = dbManager.getMeasurementDataSize();
        double indexKB = dbManager.getMeasurementDataIndexSize();

        // close DB connection
        dbManager.closeConnection();

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        double durationSecond = durationMillis / 1000;

        info = "\n[---DONE---] SCENARIO-INSERT *END: (" + dbType
                + ") Execution duration: " + durationSecond + " seconds ("
                + durationMillis + " milliseocnds)";
        info += "\n" + dbType + " DB SIZE: " + (sizeKB / 1024) + "MB ("
                + (sizeKB / 1024 / 1024) + " GB /" + sizeKB + " KB)";
        info += "\n" + dbType + " DB INDEX SIZE: " + (indexKB / 1024) + "MB ("
                + (indexKB / 1024 / 1024) + " GB /" + indexKB + " KB)";

        this.dbInsertThread.clear();
        this.staticObserver.update(info, 0);
    }

    /**
     * Query from DB in multi- threads
     */
    private void dbReadThreadLaunch(ScenarioUnit su)
    {

        DBType dbType = DBType.valueOf(su.getDatabaseType());

        IStorageManager dbManager = DBFactory.buildDBManager(dbType, su.getDatabaseConnection(),
                su.getDatabaseUsername(), su.getDatabasePassword());

        Calendar cal = Calendar.getInstance();
        long startTime = System.currentTimeMillis();

        String info = "\n------------ SCENARIO-READ ++START: " + " @Datetime: "
                + dateFormat.format(cal.getTime());
        info += "\nParam: [dbType]: " + dbType + "; " + ";";

        this.staticObserver.update(info);

        /*
         * prepare all threads for query task
         */
        this.dbQueryThread.clear();
        for (ScenarioStatement stat : su.getScenarioStatement())
        {
            OperationType operType = OperationType.valueOf(stat
                    .getOperationtype());

            if (OperationType.SELECT.equals(operType))
            {

                int numSelect = stat.getRepeat();
                int numSelectPerThread = numSelect / su.getNumOfQueryThread();

                this.dbQueryThread.add(new DBQueryThread(dbManager,
                        numSelectPerThread,
                        DBType.valueOf(su.getDatabaseType()),
                        InteroperateType.valueOf(stat.getInteroperate()),
                        stat.getContent()));
            }
        }

        // initial a DB connection
        dbManager.initConnection();

        /*
         * start all threads for query task
         */
        for (StorageCRUDThread thread : this.dbQueryThread)
        {
            thread.start();
        }

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

        // close DB connection
        dbManager.closeConnection();

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        double durationSecond = durationMillis / 1000;

        info = "\n[---DONE---] SCENARIO-READ +END: ("
                + ") Execution duration: " + durationSecond + " seconds ("
                + durationMillis + " milliseocnds)"
                + "; Total Query duration: " + (totalQueryTime / 1000)
                + " seconds (" + totalQueryTime + " milliseocnds) "
                + "; Total Query+Fetch duration: "
                + (totalQueryFetchTime / 1000) + " seconds ("
                + totalQueryFetchTime + " milliseocnds) " + ".";

        this.dbQueryThread.clear();
        this.staticObserver.update(info, 0);

    }

    /**
     * Create DB/table in single thread
     */
    public void dbCreatScenario(ScenarioUnit su)
    {

        DBType dbType = DBType.valueOf(su.getDatabaseType());

        IStorageManager dbManager = DBFactory.buildDBManager(dbType, su.getDatabaseConnection(),
                su.getDatabaseUsername(), su.getDatabasePassword());

        /*
         * initialize connection, db, and table/collection
         */
        dbManager.initConnection();

        Calendar cal = Calendar.getInstance();
        System.out.println("DB create operation time: "
                + dateFormat.format(cal.getTime()));

        for (ScenarioStatement stat : su.getScenarioStatement())
        {

            OperationType operType = OperationType.valueOf(stat
                    .getOperationtype());

            if (OperationType.CREATE.equals(operType))
            {
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

        DBType dbType = DBType.valueOf(su.getDatabaseType());

        IStorageManager dbManager = DBFactory.buildDBManager(dbType, su.getDatabaseConnection(),
                su.getDatabaseUsername(), su.getDatabasePassword());

        /*
         * initialize connection, db, and table/collection
         */
        dbManager.initConnection();

        Calendar cal = Calendar.getInstance();
        System.out.println("DB delete operation time: "
                + dateFormat.format(cal.getTime()));

        for (ScenarioStatement stat : su.getScenarioStatement())
        {

            OperationType operType = OperationType.valueOf(stat
                    .getOperationtype());

            if (OperationType.DELETE.equals(operType))
            {
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

    /**
     * Check whether a given DB CRUD thread is completed
     * 
     * @param threads
     * @return
     */
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

    public ScenarioResult getScenarioResult()
    {
        return scenarioResult;
    }

    public void setScenarioResult(ScenarioResult scenarioResult)
    {
        this.scenarioResult = scenarioResult;
    }

    public List<String> getAllScenarios()
    {
        return allScenarios;
    }

    public void setAllScenarios(List<String> allScenarios)
    {
        this.allScenarios = allScenarios;
    }
}
