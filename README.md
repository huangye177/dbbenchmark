# What is dbbenchmark

dbbenchmark is a database solution benchmark performance comparison toolkits, wherein flexible scenarios can be built and organized. 

## How-to

*   Run with source code directly (after git clone, under project root directory)

	./gradlew clean build run 

(modify file "scenariosetting.json" to change scenario setting, and run above SHELL command again)

===========

OR

===========

*   Run with compiled all-in-one jar file (after git clone, under project root directory)

	./gradlew clean build fatJar

Afterwars, an all-in-one jar file, together with its configuration JSON file, will be generated in build/libs.

Those two generated files can be copied together to any other locations, and executed via following java commands: 

	java -jar build/libs/dbbenchmark-0.1.0.jar

(modify file "build/libs/scenariosetting.json" to change scenario setting, and run above JAVA command again)


## Supported Database and/or Database engines:

*   MySQL/MariaDB (MyISAM, InnoDB, TokuDB)

*   MongoDB

===========

## MySQL configuration Tips

Following are my my.cnf to ensure mysql proper performance (tokudb related setting is also included):

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

