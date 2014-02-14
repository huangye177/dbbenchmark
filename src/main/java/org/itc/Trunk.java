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
import org.itc.data.MongoDBManager;
import org.itc.data.MySQLManager;
import org.itc.model.DBFactory;
import org.itc.model.DBType;
import org.itc.model.InteroperateType;
import org.itc.model.OperationType;
import org.itc.scenario.IObserver;
import org.itc.scenario.JSONSettingReaderWriter;
import org.itc.scenario.ScenarioStatement;
import org.itc.scenario.ScenarioUnit;
import org.itc.scenario.Scenarios;
import org.itc.scenario.StaticObserver;
import org.itc.scenario.TraceObserver;

public class Trunk {
	private static final Logger LOGGER = Logger.getLogger("Trunk");

	private IObserver traceObserver = new TraceObserver();
	private IObserver staticObserver = new StaticObserver();

	/** imported simulation scenarios from external setting file */
	private Scenarios scenarios = null;

	List<StorageCRUDThread> dbInsertThread = null;
	List<StorageCRUDThread> dbQueryThread = null;

	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

	Date today = new Date();

	public Trunk() {
		LOGGER.info("Hello, Gradle Application!");
		this.scenarios = JSONSettingReaderWriter.launchScenarioSetting();

		System.exit(0);

		this.dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
		this.dbInsertThread = new ArrayList<StorageCRUDThread>();
		this.dbQueryThread = new ArrayList<StorageCRUDThread>();
	}

	public static void main(String[] args) {
		Trunk scenarioTrunk = new Trunk();
		scenarioTrunk.getTraceObserver().clearsysLog();
		scenarioTrunk.getStaticObserver().clearsysLog();

		scenarioTrunk.runScenarios();
	}

	public void runScenarios() {

		for (ScenarioUnit su : scenarios.getScenarioUnits()) {
			/*
			 * NOTICE: Batch mode should run with Partition mode to given better
			 * performance with better compability
			 */
			boolean enableDBCreate = su.isEnableDBCreate();
			boolean enableDBInsert = su.isEnableDBInsert();
			boolean enableDBQuery = su.isEnableDBQuery();
			boolean enableDBDeletion = su.isEnableDBDeletion();

			// create DB
			if (enableDBCreate) {
				this.dbCreatScenario(su);
			}

			// insert tests
			if (enableDBInsert) {
				this.dbInsertThreadLaunch(su);
			}

			/*
			 * make main thread to sleep a while to ensure MongoDB memory-dish
			 * flush is done
			 */
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			// query DB with different query options
			if (enableDBQuery) {
				this.dbReadThreadLaunch(su);
			}

			// delete data
			if (enableDBDeletion) {
				this.dbDeleteScenario(su);
			}
		}
	}

	/**
	 * Insert into DB in multi- threads
	 */
	private void dbInsertThreadLaunch(ScenarioUnit su) {
		DBType dbType = DBType.valueOf(su.getDbType());

		IStorageManager dbManager = DBFactory.buildDBManager(dbType);

		dbManager.initConnection(su.getDatabaseConnection(),
				su.getDatabaseUsername(), su.getDatabasePassword());

		Calendar cal = Calendar.getInstance();
		long startTime = System.currentTimeMillis();

		String info = "\n------------ SCENARIO-INSERT **START: "
				+ " @Datetime: " + dateFormat.format(cal.getTime());
		info += "\nParam: [dbType]: " + dbType;
		this.staticObserver.update(info);

		// launch each insert operation with single-/multi- thread
		this.dbInsertThread.clear();
		for (ScenarioStatement stat : su.getScenarioStatement()) {

			OperationType operType = OperationType.valueOf(stat
					.getOperationtype());

			if (OperationType.INSERT.equals(operType)) {

				int numInsert = stat.getRepeat();
				int numInsertPerThread = numInsert / su.getNumOfInsertThread();

				this.dbInsertThread.add(new DBInsertThread(dbManager,
						numInsertPerThread, 
						DBType.valueOf(su.getDbType()),
						InteroperateType.valueOf(stat.getInteroperate()), 
						stat.getContent()));
			}
		}

		// initial a DB connection
		dbManager.initConnection(su.getDatabaseConnection(),
				su.getDatabaseUsername(), su.getDatabasePassword());

		/*
		 * start all threads for insert task
		 */
		for (StorageCRUDThread thread : this.dbInsertThread) {
			thread.start();
		}

		/*
		 * check results of all threads for insert task
		 */
		while (!this.isThreadsExecuted(this.dbInsertThread)) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
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
	private void dbReadThreadLaunch(ScenarioUnit su) {

		DBType dbType = DBType.valueOf(su.getDbType());

		IStorageManager dbManager = DBFactory.buildDBManager(dbType);

		dbManager.initConnection(su.getDatabaseConnection(),
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
		for (ScenarioStatement stat : su.getScenarioStatement()) {
			OperationType operType = OperationType.valueOf(stat
					.getOperationtype());

			if (OperationType.SELECT.equals(operType)) {

				int numSelect = stat.getRepeat();
				int numSelectPerThread = numSelect / su.getNumOfQueryThread();

				this.dbQueryThread.add(new DBQueryThread(dbManager,
						numSelectPerThread, 
						DBType.valueOf(su.getDbType()),
						InteroperateType.valueOf(stat.getInteroperate()), 
						stat.getContent()));
			}
		}

		// initial a DB connection
		dbManager.initConnection(su.getDatabaseConnection(),
				su.getDatabaseUsername(), su.getDatabasePassword());

		/*
		 * start all threads for query task
		 */
		for (StorageCRUDThread thread : this.dbQueryThread) {
			thread.start();
		}

		/*
		 * check results of all threads for query task
		 */
		while (!this.isThreadsExecuted(this.dbQueryThread)) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		long totalQueryTime = 0;
		long totalQueryFetchTime = 0;
		for (StorageCRUDThread thread : this.dbQueryThread) {
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
	public void dbCreatScenario(ScenarioUnit su) {

		DBType dbType = DBType.valueOf(su.getDbType());

		IStorageManager dbManager = DBFactory.buildDBManager(dbType);

		/*
		 * initialize connection, db, and table/collection
		 */
		dbManager.initConnection(su.getDatabaseConnection(),
				su.getDatabaseUsername(), su.getDatabasePassword());

		Calendar cal = Calendar.getInstance();
		System.out.println("DB create operation time: "
				+ dateFormat.format(cal.getTime()));

		for (ScenarioStatement stat : su.getScenarioStatement()) {

			OperationType operType = OperationType.valueOf(stat
					.getOperationtype());

			if (OperationType.CREATE.equals(operType)) {
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
	public void dbDeleteScenario(ScenarioUnit su) {

		DBType dbType = DBType.valueOf(su.getDbType());

		IStorageManager dbManager = DBFactory.buildDBManager(dbType);

		/*
		 * initialize connection, db, and table/collection
		 */
		dbManager.initConnection(su.getDatabaseConnection(),
				su.getDatabaseUsername(), su.getDatabasePassword());

		Calendar cal = Calendar.getInstance();
		System.out.println("DB delete operation time: "
				+ dateFormat.format(cal.getTime()));

		for (ScenarioStatement stat : su.getScenarioStatement()) {

			OperationType operType = OperationType.valueOf(stat
					.getOperationtype());

			if (OperationType.DELETE.equals(operType)) {
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
	private boolean isThreadsExecuted(List<StorageCRUDThread> threads) {
		boolean isAllExecuted = true;

		for (StorageCRUDThread thread : threads) {
			isAllExecuted = (isAllExecuted && thread.isExecutionReady());
		}

		return isAllExecuted;
	}

	public IObserver getTraceObserver() {
		return traceObserver;
	}

	public IObserver getStaticObserver() {
		return staticObserver;
	}

	public Scenarios getScenarios() {
		return scenarios;
	}

	public void setScenarios(Scenarios scenarios) {
		this.scenarios = scenarios;
	}
}
