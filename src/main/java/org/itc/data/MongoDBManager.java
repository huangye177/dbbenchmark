package org.itc.data;

import java.util.ArrayList;
import java.util.Date;
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
    String dbName = "";
    String collectionName = "";

    private MongoClient mongoClient = null;
    private DB irisitestdb = null;
    DBCollection gmMeasurementCollection = null;

    HashMap<Long, DBCollection> gmMeasurementCollectionMap = new HashMap<Long, DBCollection>();

    private long amount_of_records = 0;

    public MongoDBManager(String... args)
    {
        super();

        if (args.length >= 1 && args[0] != null)
        {
            this.dbName = args[0];
        }

        if (args.length >= 2 && args[1] != null)
        {
            this.collectionName = args[1];
        }

    }

    public MongoDBManager(long amount_of_records, boolean isTableSharding)
    {
        super();
        this.amount_of_records = amount_of_records;
    }

    @Override
    public void initConnection()
    {
        this.mongoClient = MongoDBConnectionManager.getMongoDBConnection();
        this.irisitestdb = mongoClient.getDB(dbName);
    }

    @Override
    public void closeConnection()
    {
        /*
         * No need to close MongoDB connection since it is automatically managed
         * by MongoClient
         */
        this.irisitestdb = null;
        this.gmMeasurementCollection = null;

    }

    @Override
    @Deprecated
    public void createMeasurementTable()
    {
        // gmMeasurementCollection.ensureIndex(new
        // BasicDBObject("fkDataSeriesId", 1), "Index1");
        // gmMeasurementCollection.ensureIndex(new BasicDBObject("measDateUtc",
        // 1), "Index3");

        System.out.println("CREATING MongoDB Collections...");

        irisitestdb.createCollection(collectionName, null);
        gmMeasurementCollection.ensureIndex(new BasicDBObject("fkDataSeriesId", 1).append("measDateUtc", 1), "Index2");

    }

    @Override
    @Deprecated
    public void insertMeasurements()
    {
        for (int i = 0; i < this.amount_of_records; i++)
        {
            Date measDate = this.getMeasDate();
            this.insertMeasurement(i, this.getProjectId(), this.getDataSeriesId(), measDate, measDate, this.getMeasValue());
        }
    }

    @Override
    @Deprecated
    public void insertMeasurementsInBatch()
    {
        // TODO Auto-generated method stub

    }

    @Override
    @Deprecated
    public long[] selectMeasurementByDataSeriesId()
    {
        long queryTime = System.currentTimeMillis();
        long queryFetchTime = System.currentTimeMillis();

        int dsId = this.getDataSeriesId();

        BasicDBObject query = new BasicDBObject("fkDataSeriesId", dsId);

        DBCursor cursor = null;
        cursor = this.gmMeasurementCollection.find(query);

        queryTime = System.currentTimeMillis() - queryTime;

        try
        {
            while (cursor.hasNext())
            {
                DBObject resultObject = cursor.next();
                resultObject.get("_id");
                resultObject.get("project_id");
                resultObject.get("fkDataSeriesId");
                resultObject.get("measDateUtc");
                resultObject.get("measvalue");
                // System.out.println("$$$ " + resultObject);
            }

            this.notifyObservers();

        }
        finally
        {
            cursor.close();
        }

        queryFetchTime = System.currentTimeMillis() - queryFetchTime;

        return new long[] { queryTime, queryFetchTime };
    }

    @Deprecated
    @Override
    public long[] selectMeasurementByProjectId()
    {
        long queryTime = System.currentTimeMillis();
        long queryFetchTime = System.currentTimeMillis();

        BasicDBObject query = new BasicDBObject("project_id", this.getProjectId());

        DBCursor cursor = this.gmMeasurementCollection.find(query);

        queryTime = System.currentTimeMillis() - queryTime;

        try
        {
            while (cursor.hasNext())
            {
                DBObject resultObject = cursor.next();
                resultObject.get("_id");
                resultObject.get("project_id");
                resultObject.get("fkDataSeriesId");
                resultObject.get("measDateUtc");
                resultObject.get("measvalue");
                // System.out.println("$$$ " + resultObject);
            }

            this.notifyObservers();
        }
        finally
        {
            cursor.close();
        }

        queryFetchTime = System.currentTimeMillis() - queryFetchTime;

        return new long[] { queryTime, queryFetchTime };
    }

    @Override
    @Deprecated
    public void dropMeasurementTable()
    {
        // this.gmMeasurementCollection.dropIndexes();
        // this.gmMeasurementCollection.drop();
        this.mongoClient.dropDatabase(dbName);
    }

    @Override
    public double getMeasurementDataSize()
    {
        if (this.irisitestdb == null)
        {
            return 0.0;
        }
        else
        {
            Object sizeObject = this.irisitestdb.getStats().get("dataSize");
            if (sizeObject == null)
            {
                return 0.0;
            }
            else
            {
                return Double.valueOf(this.irisitestdb.getStats().get("dataSize").toString()) / 1024;
            }
        }

    }

    @Override
    public double getMeasurementDataIndexSize()
    {
        if (this.irisitestdb == null)
        {
            return 0.0;
        }
        else
        {
            Object sizeObject = this.irisitestdb.getStats().get("indexSize");
            if (sizeObject == null)
            {
                return 0.0;
            }
            else
            {
                return Double.valueOf(this.irisitestdb.getStats().get("indexSize").toString()) / 1024;
            }
        }

    }

    /************************
     * private util methods
     ************************/

    @Deprecated
    private void insertMeasurement(long id, int projectId, long fkDataSeriesId, Date measDateUtc, Date measDateSite, double measvalue)
    {

        if (this.primaryIdAutoIncrement)
        {
            this.gmMeasurementCollection.insert(this.getMeasurementObject(projectId, fkDataSeriesId, measDateUtc, measDateSite, measvalue));
        }
        else
        {
            this.gmMeasurementCollection.insert(this.getMeasurementObject(id, projectId, fkDataSeriesId, measDateUtc, measDateSite, measvalue));
        }

    }

    @Deprecated
    private BasicDBObject getMeasurementObject(long id, int projectId, long fkDataSeriesId, Date measDateUtc, Date measDateSite, double measvalue)
    {
        BasicDBObject measureementDocument = new BasicDBObject();
        measureementDocument.put("_id", String.valueOf(id));
        measureementDocument.put("project_id", projectId);
        measureementDocument.put("fkDataSeriesId", fkDataSeriesId);
        measureementDocument.put("measDateUtc", measDateUtc);
        measureementDocument.put("measDateSite", measDateSite);
        measureementDocument.put("measvalue", measvalue);
        measureementDocument.put("refMeas", 0);
        measureementDocument.put("reliability", 1.0);

        return measureementDocument;
    }

    private BasicDBObject getMeasurementObject(int projectId, long fkDataSeriesId, Date measDateUtc, Date measDateSite, double measvalue)
    {
        BasicDBObject measureementDocument = new BasicDBObject();
        measureementDocument.put("project_id", projectId);
        measureementDocument.put("fkDataSeriesId", fkDataSeriesId);
        measureementDocument.put("measDateUtc", measDateUtc);
        measureementDocument.put("measDateSite", measDateSite);
        measureementDocument.put("measvalue", measvalue);
        measureementDocument.put("refMeas", 0);
        measureementDocument.put("reliability", 1.0);

        return measureementDocument;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execCreateOperation(Object content)
    {
        Map<String, Object> contentMap = (HashMap<String, Object>) content;

        String collectionName = (String) contentMap.get("collectionname");
        List<String> indexNames = (ArrayList<String>) contentMap.get("collectionindex");

        DBCollection collection = irisitestdb.createCollection(collectionName, null);

        BasicDBObject indexObject = new BasicDBObject();
        for (int i = 0; i < indexNames.size(); i++)
        {
            indexObject.append(indexNames.get(i), 1);
        }

        collection.ensureIndex(indexObject, "default_index");

    }

    @Override
    @SuppressWarnings("unchecked")
    public void execInsertOperation(Object content)
    {
        Map<String, Object> contentMap = (HashMap<String, Object>) content;

        String collectionName = (String) contentMap.get("collectionname");
        BasicDBObject document = (BasicDBObject) contentMap.get("document");

        DBCollection collection = irisitestdb.getCollection(collectionName);

        collection.insert(document);

    }

    @Override
    @SuppressWarnings("unchecked")
    public void execSelectOperation(Object content)
    {
        Map<String, Object> contentMap = (HashMap<String, Object>) content;

        String collectionName = (String) contentMap.get("collectionname");
        BasicDBObject document = (BasicDBObject) contentMap.get("document");

        DBCollection collection = irisitestdb.getCollection(collectionName);

        DBCursor cursor = null;
        cursor = collection.find(document);

        try
        {
            while (cursor.hasNext())
            {
                DBObject resultObject = cursor.next();
                resultObject.toString();
            }

            this.notifyObservers();

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

        DBCollection collection = irisitestdb.getCollection(collectionName);

        if (collection != null && (documents == null || documents.size() == 0))
        {
            collection.drop();
        } else if (collection != null && documents != null && documents.size() != 0) {
            for(BasicDBObject doc : documents) {
                collection.remove(doc);
            }
        }

    }

}
