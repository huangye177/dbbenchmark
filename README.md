# What is dbbenchmark

dbbenchmark is a database solution benchmark performance comparison toolkits, wherein flexible scenarios can be built and organized. 

## How-to

*   Optional (if test against MySQL with default setting): Create a MySQL database named "testdb", and a user with both name and password as "testdb".

*   Optional (if test against MongoDB): Start MongoDB server

*   Run with source code directly (after git clone, under project root directory)

	`./gradlew clean build jettyRunWar` 
	
And visit your browser at your localhost: http://localhost:8080/dbbenchmark/


Again, do NOT forget 
------
> You will need to create proper database and authorized user in advanced; the default scenario of dbbenchmark is running on MySQL MyISAM and InnoDB, with a database named "testdb", and a user with both username and password as "testdb". -- Please create them before you run the default scenario.

===========

## Supported Database and/or Database engines in example scenario files (dbbenchmark/scenarios):

*   MySQL/MariaDB (MyISAM, InnoDB, TokuDB)

*   MongoDB

===========

## MySQL configuration Tips

Following are tested my.cnf to ensure mysql proper performance (tokudb related setting is also included):

	tokudb_commit_sync=0

	max_allowed_packet = 1M
	table_open_cache = 128
	read_buffer_size = 2M
	read_rnd_buffer_size = 8M
	myisam_sort_buffer_size = 64M
	thread_cache_size = 8
	query_cache_size = 32M
	thread_concurrency = 8

	innodb_flush_log_at_trx_commit=2
	innodb_buffer_pool_size = 2G
	innodb_additional_mem_pool_size = 20M
	innodb_log_buffer_size = 8M
	innodb_lock_wait_timeout = 50
	
===========

## Source Code Structure

### Trunk.java

This is the main class which launches the program and invokes other components. Its constructor loads all "Scenarios" from either a scenario-configuration file or web page input from user, and builds a List of insert threads and a List of query threads for multi-thread pattern simulation execution. 

Its "runScenarios()" method initializes "ScenarioUnitResult" for each "ScenarioUnit", initializes a "IStorageManager" connection, initializes "ScenarioStatementResult" for each "ScenarioStatement" (from a "ScenarioUnit"), sets the number of (insert- or query-)operations per thread, starts all threads for each "ScenarioStatement", waits for all threads' completion to get the results, and closes the "IStorageManager" connection when all "ScenarioUnit" is finished. 

## Scenario-*.java

Each time when a simulation is started, it contains a list of "ScenarioUnit" (ScenarioUnit.java), wherein each "ScenarioUnit" has a list of "ScenarioStatement" (ScenarioStatement.java). Each "ScenarioStatement" contains the operatoin type (insert/query/delete), the number of repeat (namely number of insert/query), content of operation (e.g., concerned sql statement), and so on. Further more, to collect the simulation results, such as start time, end time, and execution duration, corresponding result collecting classes, including ScenarioUnitResult.java and ScenarioStatementResult.java, are also supplied.

## StorageCRUDThread.java

A Thread which is inherited by DBInsertThread.java and DBQueryThread.java. "StorageCRUDThread" invokes the "IDataInteroperator" to interoperate given parameters (e.g., operation type, DB type, and statement content) into an object, which can be executed by different children class of "IStorageManager", and invokes the "IStorageManager" to access DB in a propogated thread.

## IDataInteroperator.java

An interroperator to interoperate given statment content (from "ScenarioStatement") into an object, which can be understood by a "IStorageManager" implementations according to other given parameters, such as DB type, operation type, and interoperator type.

## IStorageManager.java

The DB operation abstract class, which can be inherited (such as MongoDBManager.java and MySQLManager.java) to supply CRUD operations to different types of data storage.
