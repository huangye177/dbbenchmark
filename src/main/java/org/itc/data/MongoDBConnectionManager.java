package org.itc.data;

import java.net.UnknownHostException;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

/**
 * The MongoClient instance actually represents a pool of connections to the
 * database; thus will only need one instance of class MongoClient even with
 * multiple threads. See the concurrency doc page for more information.
 * 
 * @author ye huang
 */
public class MongoDBConnectionManager
{
    private final static String IRIS_MONGODB = "localhost";
    private static MongoClient mongoClient = null;

    private MongoDBConnectionManager()
    {

    }

    public static MongoClient getMongoDBConnection()
    {
        if (mongoClient == null)
        {
            try
            {
                mongoClient = new MongoClient(IRIS_MONGODB);
                mongoClient.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
                System.out.println("MongoDB Write Concern: " + mongoClient.getWriteConcern().toString());
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
        }
        return mongoClient;
    }
}
