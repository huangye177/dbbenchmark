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
            return originalContent;

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

    private Object interoperateMongoDBInsert(Object content)
    {
        return this.injectRandomIntIntoDocument(content);
    }

    private Object interoperateMongoDBSelect(Object content)
    {
        return this.injectRandomIntIntoDocument(content);
    }

    @SuppressWarnings("unchecked")
    private Object injectRandomIntIntoDocument(Object content)
    {
        Map<String, Object> contentMap = (HashMap<String, Object>) content;
        List<String> documents = (ArrayList<String>) contentMap.get("document");

        BasicDBObject dbObject = new BasicDBObject();
        for (String doc : documents)
        {
            dbObject.append(doc, this.getRandomDouble());
        }

        contentMap.put("document", dbObject);

        return content;
    }

    protected int getRandomDouble()
    {
        return this.generator.nextInt(this.generatorMax);
    }

}
