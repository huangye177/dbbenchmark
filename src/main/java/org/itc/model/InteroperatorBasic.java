package org.itc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.mongodb.BasicDBObject;

public class InteroperatorBasic extends IDataInteroperator
{
    private Random generator = new Random();
    final private int generatorMax = 1000;

    @Override
    public Object interoperate(Object originalContent, DBType dbtype, OperationType operType)
    {
        if (DBType.MYSQL.equals(dbtype))
        {
            if (OperationType.INSERT.equals(operType))
            {
                return this.interoperateMySQLInsert(originalContent);
            }
            else if (OperationType.SELECT.equals(operType))
            {
                return this.interoperateMySQLSelect(originalContent);
            }
            else
            {
                return originalContent;
            }

        }
        else if (DBType.MONGODB.equals(dbtype))
        {
            if (OperationType.INSERT.equals(operType))
            {
                return this.interoperateMongoDBInsert(originalContent);
            }
            else if (OperationType.SELECT.equals(operType))
            {
                return this.interoperateMongoDBSelect(originalContent);
            }
            else
            {
                return originalContent;
            }
        }
        else
        {
            return originalContent;
        }
    }

    private Object interoperateMySQLSelect(Object originalContent)
    {
        String var = ((String) originalContent).replaceFirst("\\?", String.valueOf(this.getRandomDouble()));

        return var;
    }

    private Object interoperateMySQLInsert(Object originalContent)
    {
        String var = ((String) originalContent).replaceFirst("\\?", String.valueOf(this.getRandomDouble()))
                .replaceFirst("\\?", String.valueOf(this.getRandomDouble()));

        return var;
    }

    @SuppressWarnings("unchecked")
    private Object interoperateMongoDBInsert(Object originalContent)
    {
        Map<String, Object> originalContentMap = (HashMap<String, Object>) originalContent;
        List<String> documents = (ArrayList<String>) originalContentMap.get("document");

        BasicDBObject dbObject = new BasicDBObject();
        for (String doc : documents)
        {
            dbObject.append(doc, this.getRandomDouble());
        }

        // create the to-return result
        Map<String, Object> returnContentMap = new HashMap<String, Object>(originalContentMap);
        returnContentMap.put("document", dbObject);

        return returnContentMap;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object interoperateMongoDBSelect(Object originalContent)
    {
        Map<String, Object> originalContentMap = (HashMap<String, Object>) originalContent;
        List<ArrayList> documents = (ArrayList<ArrayList>) originalContentMap.get("document");

        // find query condition attributes & to-return attributes
        List<String> conditions = documents.get(0);
        List<String> returns = documents.get(1);

        BasicDBObject conditionObject = new BasicDBObject();
        for (String doc : conditions)
        {
            conditionObject.append(doc, this.getRandomDouble());
        }

        BasicDBObject returnsObject = new BasicDBObject();
        returnsObject.append("_id", 0);
        for (String doc : returns)
        {
            returnsObject.append(doc, 1);
        }

        // create the to-return result
        Map<String, Object> returnContentMap = new HashMap<String, Object>(originalContentMap);

        returnContentMap.put("conditions", conditionObject);
        returnContentMap.put("returns", returnsObject);

        return returnContentMap;
    }

    protected int getRandomDouble()
    {
        return this.generator.nextInt(this.generatorMax);
    }

}
