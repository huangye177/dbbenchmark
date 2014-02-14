package org.itc.model;

import org.itc.data.IStorageManager;

public class DBFactory {

	public static IStorageManager buildDBManager(DBType dbType) {
		
		IStorageManager dbManager = null;
		
		switch (dbType) {
			case MYSQL: {
				dbManager = null;
				break;
			}
			case MONGODB: {
				dbManager = null;
				break;
			}
			default: {
				break;
			}
		}
		return dbManager;
	}
}
