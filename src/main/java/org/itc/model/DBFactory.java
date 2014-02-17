package org.itc.model;

import org.itc.data.IStorageManager;
import org.itc.data.MongoDBManager;
import org.itc.data.MySQLManager;

public class DBFactory
{

    public static IStorageManager buildDBManager(DBType dbType, String... args)
    {

        IStorageManager dbManager = null;

        switch (dbType)
        {
            case MYSQL:
            {
                dbManager = new MySQLManager(args);
                break;
            }
            case MONGODB:
            {
                dbManager = new MongoDBManager(args);
                break;
            }
            default:
            {
                break;
            }
        }
        return dbManager;
    }
}
