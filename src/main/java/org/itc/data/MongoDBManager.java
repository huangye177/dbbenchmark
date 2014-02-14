package org.itc.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

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
    final String IRIS_TEST_DB = "iristestdb";
    final String IRIS_TEST_COLLECTION = "gm_std_measurements";

    private MongoClient mongoClient = null;
    private DB irisitestdb = null;
    DBCollection gmMeasurementCollection = null;

    HashMap<Long, DBCollection> gmMeasurementCollectionMap = new HashMap<Long, DBCollection>();

    private long amount_of_records = 0;

    public MongoDBManager()
    {
        super();
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
        this.irisitestdb = mongoClient.getDB(IRIS_TEST_DB);

            this.gmMeasurementCollection = irisitestdb.getCollection(IRIS_TEST_COLLECTION);
        
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
    public void createMeasurementTable()
    {
        // gmMeasurementCollection.ensureIndex(new
        // BasicDBObject("fkDataSeriesId", 1), "Index1");
        // gmMeasurementCollection.ensureIndex(new BasicDBObject("measDateUtc",
        // 1), "Index3");

        System.out.println("CREATING MongoDB Collections...");

        
            irisitestdb.createCollection(IRIS_TEST_COLLECTION, null);
            gmMeasurementCollection.ensureIndex(new BasicDBObject("fkDataSeriesId", 1).append("measDateUtc", 1), "Index2");
        
    }

    @Override
    public void insertMeasurements()
    {
        for (int i = 0; i < this.amount_of_records; i++)
        {
            Date measDate = this.getMeasDate();
            this.insertMeasurement(i, this.getProjectId(), this.getDataSeriesId(), measDate, measDate, this.getMeasValue());
        }
    }

    @Override
    public void insertMeasurementsInBatch()
    {
        // TODO Auto-generated method stub

    }

    @Override
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
    public void dropMeasurementTable()
    {
        // this.gmMeasurementCollection.dropIndexes();
        // this.gmMeasurementCollection.drop();
        this.mongoClient.dropDatabase(IRIS_TEST_DB);
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
	public void execCreateOperation(String content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execInsertOperation(String content, int repeation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execSelectOperation(String content, int repeation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDeleteOperation(String content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDropOperation(String content) {
		// TODO Auto-generated method stub
		
	}

}
