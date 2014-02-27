# What is dbbenchmark

dbbenchmark is a database solution benchmark performance comparison toolkits, wherein flexible scenarios can be built and organized. 

## How-to

*   Create a MySQL database named "testdb", and a user with both name and password as "testdb".

*   Run with source code directly (after git clone, under project root directory)

	`./gradlew clean build jettyRunWar` 
	
And visit your browser at your localhost: http://localhost:8080/dbbenchmark/

## Again, do NOT forget 

You will need to create proper database and authorized user in advanced; the default scenario of dbbenchmark is running on MySQL MyISAM and InnoDB, with a database named "testdb", and a user with both username and password as "testdb". -- Please create them before you run the default scenario.

## Supported Database and/or Database engines in example scenario files (dbbenchmark/scenarios):

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

