package org.itc.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// import org.json.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * CRUD Manager of MongoDB
 * 
 * @author ye huang
 * @datetime 2013 12:51:35 PM
 */
public class MongoDBManager extends IStorageManager
{
    private MongoClient mongoClient = null;
    private DB mongoDBDataBase = null;
    private String mongoDBName = "";

    public MongoDBManager(String... args)
    {
        super();

        if (args.length >= 1 && args[0] != null)
        {
            this.mongoDBName = args[0];
        }
    }

    public MongoDBManager(long amount_of_records, boolean isTableSharding)
    {
        super();
    }

    @Override
    public void initConnection()
    {
        this.mongoClient = MongoDBConnectionManager.getMongoDBConnection();
        this.mongoDBDataBase = mongoClient.getDB(mongoDBName);
    }

    @Override
    public void closeConnection()
    {
        /*
         * No need to close MongoDB connection since it is automatically managed
         * by MongoClient
         */
        this.mongoDBDataBase = null;

    }

    @Override
    @SuppressWarnings("unchecked")
    public void execCreateOperation(Object content)
    {
        Map<String, Object> contentMap = (HashMap<String, Object>) content;

        String collectionName = (String) contentMap.get("collectionname");
        List<String> indexNames = (ArrayList<String>) contentMap.get("collectionindex");

        DBCollection collection = mongoDBDataBase.createCollection(collectionName, null);

        BasicDBObject indexObject = new BasicDBObject();
        for (int i = 0; i < indexNames.size(); i++)
        {
            indexObject.append(indexNames.get(i), 1);
        }

        collection.ensureIndex(indexObject, "default_mongodb_test_index");

    }

    @Override
    @SuppressWarnings("unchecked")
    public void execInsertOperation(Object content)
    {
        Map<String, Object> contentMap = (HashMap<String, Object>) content;

        String collectionName = (String) contentMap.get("collectionname");
        BasicDBObject document = (BasicDBObject) contentMap.get("document");

        DBCollection collection = mongoDBDataBase.getCollection(collectionName);

        collection.insert(document);

    }

    @Override
    @SuppressWarnings("unchecked")
    public void execSelectOperation(Object content)
    {
        Map<String, Object> contentMap = (HashMap<String, Object>) content;

        String collectionName = (String) contentMap.get("collectionname");
        // BasicDBObject document = (BasicDBObject) contentMap.get("document");
        BasicDBObject conditions = (BasicDBObject) contentMap.get("conditions");
        BasicDBObject returns = (BasicDBObject) contentMap.get("returns");

        DBCollection collection = mongoDBDataBase.getCollection(collectionName);

        DBCursor cursor = null;
        cursor = collection.find(conditions, returns);

        try
        {
            // fetch predefined number of rows from scenario
            int counter = 0;
            if (this.fetchSize > 0)
            {
                while (cursor.hasNext() && (counter < this.fetchSize))
                {
                    DBObject obj = cursor.next();
                    obj.toString();
                    counter++;
                }
            }
        }
        finally
        {
            cursor.close();
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public void execDeleteOperation(Object content)
    {
        Map<String, Object> contentMap = (HashMap<String, Object>) content;

        String collectionName = (String) contentMap.get("collectionname");
        List<BasicDBObject> documents = (ArrayList<BasicDBObject>) contentMap.get("document");

        DBCollection collection = mongoDBDataBase.getCollection(collectionName);

        if (collection != null && (documents == null || documents.size() == 0))
        {
            collection.drop();
        }
        else if (collection != null && documents != null && documents.size() != 0)
        {
            for (BasicDBObject doc : documents)
            {
                collection.remove(doc);
            }
        }

    }

    @Override
    public double getMeasurementDataSize()
    {
        if (this.mongoDBDataBase == null)
        {
            return 0.0;
        }
        else
        {
            Object sizeObject = this.mongoDBDataBase.getStats().get("dataSize");
            if (sizeObject == null)
            {
                return 0.0;
            }
            else
            {
                return Double.valueOf(this.mongoDBDataBase.getStats().get("dataSize").toString()) / 1024;
            }
        }

    }

    @Override
    public double getMeasurementDataIndexSize()
    {
        if (this.mongoDBDataBase == null)
        {
            return 0.0;
        }
        else
        {
            Object sizeObject = this.mongoDBDataBase.getStats().get("indexSize");
            if (sizeObject == null)
            {
                return 0.0;
            }
            else
            {
                return Double.valueOf(this.mongoDBDataBase.getStats().get("indexSize").toString()) / 1024;
            }
        }
    }
}
